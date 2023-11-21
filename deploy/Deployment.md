# 1. Docker 部署

DataSophon支持Docker 部署,在解压发布包后,内部自带 Dockerfile 用于构建镜像

```shell
docker build -t datasophon/datasophon:dev .
```

镜像启动成功后，在浏览器中访问 <http://docker_ip:8081/ddh> 进入登录页。 默认用户名和密码为admin/admin123

## 1.1 配置应用数据库

如果将 DataSophon 用于生产环境，建议使用 MySQL 作为应用程序数据库。配置步骤如下：

1. 新建一个名为 `datasophon.conf` 的空文件，将以下内容填写完整，然后粘贴到到文件中

```shell
# 应用数据库配置
datasource.ip=localhost       # 数据库IP或域名
datasource.port=3306          # 数据库端口
datasource.database=datasophon    # 数据库名称
datasource.username=datasophon      # 用户名
datasource.password=datasophon      # 密码

# 应用服务器配置
server.port=8081              # 服务器端口
server.address=0.0.0.0        # 服务器地址（内网地址）

```

2. 运行以下命令，使用新建的 `datasophon.conf` 配置启动镜像

```shell
docker run -d --name datasophon -v your_path/datasophon.conf:/datasophon/conf/datasophon.conf -p 8081:8081 datasophon/datasophon
```

## 1.2 DDP部署包挂载

DataSophon 还需要在DDP部署包才可以真正的进行使用,由于部署包文件较大,需要另外下载
下载成功后可以将这个路径挂载到容器外部；在启动命令中增加参数 `-v your_path/DDP:/opt/datasophon/DDP/packages` 即可。以下是完整命令：

```shell
docker run -d --name datasophon -v your_path/datasophon.conf:/datasophon/conf/datasophon.conf -v your_path/DDP:/opt/datasophon/DDP/packages -p 8081:8081 datasophon/datasophon
```

