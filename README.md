# HowTomcatWorks

# 目录

[一、简单的Web服务器](#一简单的web服务器)

- 构成
- 类说明
- 运行

[二、简单的Servlet容器](#二简单的servlet容器)

- 类图
- 类说明
- 运行

[三、连接器](#三连接器)

- 作用


- 类说明

  - connector

    - 。。。


    - http
    - startup

## 一、简单的Web服务器

### 构成
我们的服务器程序在ex01包里，由下面的三个java文件组成：
- HttpServer
- Request
- Response

### 类说明
1. HttpServer： 代表一个web服务器，能提供公共静态final变量`WEB_ROOT`所在目录和其中子目录的所有静态资源
   - await():使用while循环接收来自客户端的请求，当请求为`SHUTDOWN`时终止循环
2. Request：代表一个HTTP请求，从负责与客户端通信的Socket中传递过来的`InputStream`构造成这个类的一个实例
   - parse():读取HTTP里的请求
   - parseUri():从parse后的字符串中解析uri
3. Response： 代表一个HTTP响应，通过传递由套接字获得的`OutputStream`和`HttpServer`的`await()`来构造
   - sendStaticResource()：用来发送一个静态资源，没有则打印错误码  

### 运行

1. 启动HttpServer类
2. 打开你的浏览器并在地址栏或网址框中敲入下面的命令：http://localhost:8080/index.html 
3. 结果：
- 控制台

```
GET /index.html HTTP/1.1
Host: localhost:8080
Connection: keep-alive
Upgrade-Insecure-Requests: 1
User-Agent: Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/55.0.2883.87 Safari/537.36
Accept: text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8
DNT: 1
Accept-Encoding: gzip, deflate, sdch, br
Accept-Language: zh-CN,zh;q=0.8
Cookie: oracle.uix=0^^GMT+8:00^p; _ga=GA1.1.1778589621.1515157486; Hm_lvt_512065947708a980c982b4401d14c2f5=1515157487,1516670331
```

- 浏览器


![](https://i.loli.net/2018/02/19/5a8ade1a3c57e.png)

***

 ## 二、简单的Servlet容器 
 ### 类图
类图大致如下：
![类图](https://i.loli.net/2018/02/18/5a88bf4e12d38.png)

### 类说明
#### ex01
1. HttpServer1：和上面的HttpServer差不多，不过同时支持静态资源和Servlet
   - await()：可以将请求分发给`StaticResourceProcessor`和`ServletProcessor`，如果字符串包含`/servlet/`，请求将分发给后者
2. Request：继承自`ServletRequest`，代表一个Request对象并传给service方法
   - parse():读取HTTP里的请求
   - parseUri():从解析后的字符串中解析uri
3. Response：继承自`ServletResponse`，代表一个Response对象并传给service方法
    - sendStaticResource():用来发送一个静态资源
    - getWriter()：重写方法，使用了`PrintWriter`
4. ServletProcessor1：加载Servlet
   - process()：获取URI，创建一个类加载器并告诉类加载器加载的位置，即`Constants`指向的目录
5. StaticResourceProcessor：提供静态资源
   - process()：调用Response的`sendStaticResource()`
6. Constants：指向静态资源保存的位置

#### ex02 改进版
在`ex01`中，我们直接使用Request和Response直接向上转型为`ServletRequest `和 `ServletResponse`，这会危害到安全性。知道这点的程序员可以将`ServletRequest `和 `ServletResponse`向下转型为Request和Response，并调用他们的方法，如Request的`parseUri()`和Response的`sendStaticResource()`。一个解决方法是将它们的方法设为私有的，但这在内部就不可见了。当然可以设为默认修饰的，这样就内部可见了。但我们使用另一种更优雅的解决方法：**Facade模式**，又称门面模式、外观模式。

![Facade](https://i.loli.net/2018/02/18/5a88dea0856ae.jpg)
1. RequestFacade：构造了一个 `RequestFacade` 对象并把它传递给 `service() `方法，而不是向下转换Request 对象为` ServletRequest `对象并传递给 service 方法。Servlet 程序员仍然可以向下转换`ServletRequest `实例为 `RequestFacade`，不过它们只可以访问 `ServletRequest` 接口里边的公共方法。现在 `parseUri()` 方法就是安全的了 。

   - RequestFacade(Request request) ：将传入的Request赋值给私有的`ServletRequest`对象

2. ResponseFacade：同上

### 运行

1. 启动HttpServer类
2. 打开你的浏览器并在地址栏或网址框中敲入下面的命令：http://localhost:8080/servlet/PrimitiveServlet
3. 结果：
- 控制台

```
GET /servlet/PrimitiveServlet HTTP/1.1
Host: localhost:8080
Connection: keep-alive
Upgrade-Insecure-Requests: 1
User-Agent: Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/55.0.2883.87 Safari/537.36
Accept: text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8
DNT: 1
Accept-Encoding: gzip, deflate, sdch, br
Accept-Language: zh-CN,zh;q=0.8
Cookie: oracle.uix=0^^GMT+8:00^p; _ga=GA1.1.1778589621.1515157486; Hm_lvt_512065947708a980c982b4401d14c2f5=1515157487,1516670331


from service
```

***

## 三、连接器

Catalina 中有两个主要的模块：`连接器`和`容器`

###  作用

解析HTTP请求头部并让Servlet可以获得头部，cookie，参数名/值等。

### 类说明

StringManager：为每个包都分配一个属性文件，用来存储错误信息，一个StringManager实例被一个包下的文件共享，这是一个单例类

- getManager()：获取一个StringManager实例

- getString()：返回一个错误信息


ServletProcessor ：加载Servlet

- process()：获取URI，创建一个类加载器并告诉类加载器加载的位置，并调用Servlet的`service`方法

StaticResourceProcessor ：提供静态资源请求

- process()：调用`HttpResponse` 的`sendStaticResource()`

RequestStream：继承自`ServletInputStream `

ResponseStream ：继承自`ServletOutputStream `

ResponseWriter ：一个自动刷新的`PrintWriter`

#### http

1. HttpConnector ：连接器，继承Runable，创建一个套接字等待HTTP请求
   - run()：等待HTTP请求，为每个请求创建`HttpProcessor`实例，并调用`HttpProcessor`的`process`方法
   - start()：启动一个新线程
2. HttpProcessor ：
   - process()：
     - 创建一个`HttpRequest`对象
     - 创建一个`HttpResponse`对象
     - 解析的HTTP请求的第一行和头部，并放入`HttpRequest`对象
     - 解析`HttpRequest`和`HttpResponse`对象到`ServletProcessor `或`StaticResourceProcessor `
   - parseHeaders()：解析请求头部
   - parseRequest()：解析请求行
   - normalize()：规范化地址
3. HttpRequest：HTTP请求类
   - 读取套接字的输入流
   - 解析请求行
   - 解析头部
   - 解析cookies
   - 获取参数
4. HttpResponse：HTTP响应类
5. HttpHeader：HTTP头部类
6. RequestFacade：`HttpRequest`门面类
7. ResponseFacade：`HttpResponse`门面类
8. HttpRequestLine ：HTTP请求行类
9. SocketInputStream ：扩展InputStream以获得更高效的读取行
10. Constants：指向静态资源保存的位置

#### startup

- boostrap：启动连接器


