package com.datasophon.api.configuration;

import static com.datasophon.common.Constants.GRAFANA_PATH;

import com.datasophon.api.service.ClusterServiceDashboardService;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.SneakyThrows;

import org.eclipse.jetty.proxy.ProxyServlet;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.UpgradeRequest;
import org.eclipse.jetty.websocket.api.WebSocketAdapter;
import org.eclipse.jetty.websocket.api.WebSocketBehavior;
import org.eclipse.jetty.websocket.api.WebSocketPolicy;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.eclipse.jetty.websocket.servlet.ServletUpgradeRequest;
import org.eclipse.jetty.websocket.servlet.ServletUpgradeResponse;
import org.eclipse.jetty.websocket.servlet.WebSocketCreator;
import org.eclipse.jetty.websocket.servlet.WebSocketServlet;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.util.UriComponentsBuilder;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;

@Configuration
@ConditionalOnProperty(name = "datasophon.proxy-grafana.enable", havingValue = "true")
public class GrafanaProxyConfiguration {
    
    @Value("${datasophon.proxy-grafana.max-threads:32}")
    String maxThreads;
    
    @Autowired
    private ClusterServiceDashboardService clusterServiceDashboardService;
    
    @Bean
    public ServletRegistrationBean<Servlet> grafanaHttpProxy() {
        ServletRegistrationBean<Servlet> servlet = new ServletRegistrationBean<>(new GrafanaProxyServlet(),
                GRAFANA_PATH + "/*");
        servlet.setInitParameters(MapUtil.builder("maxThreads", maxThreads)
                .put("preserveHost", "true").build());
        return servlet;
    }
    
    public class GrafanaProxyServlet extends ProxyServlet {
        
        private static final long WEBSOCKET_DEFAULT_MAX_IDLE = 60000; // 1 minute
        private static final int WEBSOCKET_DEFAULT_MAX_BUFF = 1024 * 1024; // 1 mb
        
        private WebSocketServletFactory factory;
        
        @Override
        public void init() throws ServletException {
            super.init();
            
            try {
                WebSocketPolicy policy = new WebSocketPolicy(WebSocketBehavior.SERVER);
                ServletContext ctx = getServletContext();
                factory = WebSocketServletFactory.Loader.load(ctx, policy);
                factory.getPolicy().setIdleTimeout(WEBSOCKET_DEFAULT_MAX_IDLE);
                factory.getPolicy().setMaxBinaryMessageSize(WEBSOCKET_DEFAULT_MAX_BUFF);
                factory.getPolicy().setMaxTextMessageBufferSize(WEBSOCKET_DEFAULT_MAX_BUFF);
                factory.setCreator(new WebSocketServerCreator());
                factory.start();
                ctx.setAttribute(WebSocketServletFactory.class.getName(), factory);
            } catch (Exception x) {
                throw new ServletException(x);
            }
        }
        
        @SneakyThrows
        @Override
        protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
            String wsKey = request.getHeader("Sec-WebSocket-Key");
            if (wsKey != null) {
                if (factory.isUpgradeRequest(request, response)) {
                    if (factory.acceptWebSocket(request, response)) {
                        return;
                    }
                    if (response.isCommitted()) {
                        return;
                    }
                }
                
            }
            super.service(request, response);
        }
        
        @Override
        protected String rewriteTarget(HttpServletRequest request) {
            // 获取集群 grafana 的地址
            Integer clusterId = getCluster(request.getRequestURI());
            if (clusterId != null) {
                String host = "http://" + clusterServiceDashboardService.getGrafanaHost(clusterId);
                return UriComponentsBuilder.fromUriString(host)
                        .path(request.getRequestURI())
                        .query(request.getQueryString())
                        .build(true).toUriString();
            } else {
                return super.rewriteTarget(request);
            }
        }
        
        @Override
        public void destroy() {
            try {
                ServletContext ctx = getServletContext();
                ctx.removeAttribute(WebSocketServletFactory.class.getName());
                factory.stop();
                factory = null;
            } catch (Exception ignore) {
                // ignore;
            }
        }
    }
    
    private Integer getCluster(String requestUrl) {
        try {
            List<String> paths = StrUtil.splitTrim(requestUrl, "/");
            if (paths.size() > 3) {
                return new Integer(paths.get(2));
            } else {
                return null;
            }
        } catch (Exception e) {
            return null;
        }
    }
    
    public class WebSocketServerServlet extends WebSocketServlet {
        private final Logger log = LoggerFactory.getLogger(WebSocketServerServlet.class);
        private static final long WEBSOCKET_DEFAULT_MAX_IDLE = 60000; // 1 minute
        private static final int WEBSOCKET_DEFAULT_MAX_BUFF = 1024 * 1024; // 1 mb
        
        @Override
        public void configure(WebSocketServletFactory factory) {
            log.info("Configuring the web socket adapter");
            factory.getPolicy().setIdleTimeout(WEBSOCKET_DEFAULT_MAX_IDLE);
            factory.getPolicy().setMaxBinaryMessageSize(WEBSOCKET_DEFAULT_MAX_BUFF);
            factory.getPolicy().setMaxTextMessageBufferSize(WEBSOCKET_DEFAULT_MAX_BUFF);
            
            factory.setCreator(new WebSocketServerCreator());
        }
    }
    
    public class WebSocketServerCreator implements WebSocketCreator {
        
        private final WebSocketServerAdapter websocket = new WebSocketServerAdapter();
        
        @Override
        public Object createWebSocket(ServletUpgradeRequest servletUpgradeRequest, ServletUpgradeResponse servletUpgradeResponse) {
            for (String subprotocol : servletUpgradeRequest.getSubProtocols()) {
                if ("binary".equals(subprotocol)) {
                    servletUpgradeResponse.setAcceptedSubProtocol(subprotocol);
                }
            }
            return websocket;
        }
    }
    
    public class WebSocketServerAdapter extends WebSocketAdapter {
        private final Logger log = LoggerFactory.getLogger(WebSocketServerAdapter.class);
        
        private SslContextFactory sslContextFactory;
        private final WebSocketClient webSocketClient;
        private Session proxyingSession;
        
        private static final String WEBSOCKET_URL_FORMAT = "ws://%s%s";
        
        public WebSocketServerAdapter() {
            initSslContextFactory();
            this.webSocketClient = new WebSocketClient(this.sslContextFactory);
        }
        
        private void initSslContextFactory() {
            // initialize SslContextFactory
            this.sslContextFactory = new SslContextFactory();
            // SSLContext sslContext = ....
            // this.sslContextFactory.setSslContext(sslContext);
            
            // ArrayList<String> ciphersToEnable = new ArrayList<> (sslContext.getDefaultSSLParameters ().getCipherSuites ().length + 1);
            // ciphersToEnable.add (".*_GCM_.*"); // GCM first in case ordering is honored
            // ciphersToEnable.addAll (Arrays.asList (sslContext.getDefaultSSLParameters ().getCipherSuites ()));
            // this.sslContextFactory.setIncludeCipherSuites (ciphersToEnable.toArray (new String[ciphersToEnable.size ()]));
            // this.sslContextFactory.setExcludeCipherSuites (".*[Aa][Nn][Oo][Nn].*", ".*[Nn][Uu][Ll][Ll].*");
        }
        
        @Override
        public void onWebSocketConnect(Session sess) {
            super.onWebSocketConnect(sess);
            
            String host = clusterServiceDashboardService.getGrafanaHost(1); // target ip
            String path = sess.getUpgradeRequest().getRequestURI().getPath(); // url path
            String dest = String.format(WEBSOCKET_URL_FORMAT, host, path);
            try {
                URI destUri = new URI(dest);
                
                webSocketClient.start();
                Future<Session> future = webSocketClient.connect(new WebSocketClientAdapter(sess), destUri, getClientUpgradeRequest(sess));
                
                proxyingSession = future.get();
                if (proxyingSession != null && proxyingSession.isOpen()) {
                    log.debug("websocket connected to {}", dest);
                }
            } catch (URISyntaxException e) {
                log.error("invalid url: {}", dest);
            } catch (IOException | ExecutionException | InterruptedException e) {
                log.error("exception while connecting to {}", dest, e);
            } catch (Exception e) {
                log.error("exception while starting websocket client", e);
            }
        }
        
        @Override
        public void onWebSocketText(String message) {
            super.onWebSocketText(message);
            log.debug("websocket message received {}", message);
            // forwarding ...
            if (proxyingSession != null && proxyingSession.isOpen()) {
                try {
                    proxyingSession.getRemote().sendString(message);
                } catch (IOException e) {
                    log.error("exception while forwarding text message to client", e);
                }
            } else {
                log.error("proxying session is null or closed.");
            }
        }
        
        @Override
        public void onWebSocketBinary(byte[] payload, int offset, int len) {
            super.onWebSocketBinary(payload, offset, len);
            log.debug("websocket binary received, offset:{}, len: {}", offset, len);
            // forwarding ...
            if (proxyingSession != null && proxyingSession.isOpen()) {
                ByteBuffer byteBuffer = ByteBuffer.wrap(payload, offset, len);
                try {
                    this.proxyingSession.getRemote().sendBytes(byteBuffer);
                } catch (IOException e) {
                    log.error("exception while forwarding text message to client", e);
                }
            } else {
                log.error("proxying session is null or closed.");
            }
        }
        
        @Override
        public void onWebSocketClose(int statusCode, String reason) {
            super.onWebSocketClose(statusCode, reason);
            log.debug("Socket Closed: status code: [ {} ]", statusCode);
            disconnect();
        }
        
        private ClientUpgradeRequest getClientUpgradeRequest(Session sess) {
            ClientUpgradeRequest request = new ClientUpgradeRequest();
            
            UpgradeRequest upgradeRequest = sess.getUpgradeRequest();
            
            request.setCookies(upgradeRequest.getCookies());
            request.setSubProtocols(upgradeRequest.getSubProtocols());
            
            Map<String, List<String>> headers = upgradeRequest.getHeaders();
            headers.forEach(request::setHeader);
            return request;
        }
        
        /**
         * This method unregisters the subscriber and closes the connection
         */
        private void disconnect() {
            
            if (this.getSession() == null || !this.getSession().isOpen()) {
                return;
            }
            
            try {
                this.getSession().disconnect();
                
                if (isConnected()) {
                    log.debug("Could not disconnect the websocket client");
                }
            } catch (Exception e) {
                log.error("Exception on disconnecting the websocket client");
            } finally {
                if (this.webSocketClient != null) {
                    try {
                        this.webSocketClient.stop();
                    } catch (Exception e) {
                        log.error("Exception while stopping websocket client", e);
                    }
                }
            }
            
        }
    }
    
    public class WebSocketClientAdapter extends WebSocketAdapter {
        private final Logger log = LoggerFactory.getLogger(WebSocketClientAdapter.class);
        
        private final Session proxyingSess;
        
        WebSocketClientAdapter(Session sess) {
            this.proxyingSess = sess;
        }
        
        @Override
        public void onWebSocketConnect(Session sess) {
            super.onWebSocketConnect(sess);
            
            log.debug("websocket client connected ...");
        }
        
        @Override
        public void onWebSocketText(String message) {
            super.onWebSocketText(message);
            log.debug("websocket message received {}", message);
            // forwarding
            if (this.proxyingSess != null && this.proxyingSess.isOpen()) {
                try {
                    this.proxyingSess.getRemote().sendString(message);
                } catch (IOException e) {
                    log.error("exception while forwarding text message to client", e);
                }
            } else {
                log.error("proxying session is null or closed.");
            }
        }
        
        @Override
        public void onWebSocketBinary(byte[] payload, int offset, int len) {
            super.onWebSocketBinary(payload, offset, len);
            log.debug("websocket binary received, offset:{}, len: {}", offset, len);
            // forwarding ...
            ByteBuffer byteBuffer = ByteBuffer.wrap(payload, offset, len);
            if (this.proxyingSess != null && this.proxyingSess.isOpen()) {
                try {
                    this.proxyingSess.getRemote().sendBytes(byteBuffer);
                } catch (IOException e) {
                    log.error("exception while forwarding binary to client", e);
                }
            } else {
                log.error("proxying session is null or closed.");
            }
        }
        
        @Override
        public void onWebSocketClose(int statusCode, String reason) {
            super.onWebSocketClose(statusCode, reason);
            log.debug("Socket Closed: status code: [ {} ]", statusCode);
            disconnect();
        }
        
        /**
         * This method unregisters the subscriber and closes the connection
         */
        private void disconnect() {
            
            if (this.getSession() == null || !this.getSession().isOpen()) {
                return;
            }
            
            try {
                this.getSession().disconnect();
                
                if (isConnected()) {
                    log.debug("Could not disconnect the websocket client");
                }
            } catch (Exception e) {
                log.error("Exception on disconnecting the websocket client");
            }
            
        }
    }
    
}
