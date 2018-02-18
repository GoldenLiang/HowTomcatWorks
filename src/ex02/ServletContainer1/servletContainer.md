一个全功能的 servlet 容器会为 servlet 的每个 HTTP 请求做下面一些工作：
- 当第一次调用 servlet 的时候，加载该 servlet 类并调用 servlet 的 init 方法(仅仅一次)。
- 对每次请求，构造一个` javax.servlet.ServletRequest `实例和一个
`javax.servlet.ServletResponse `实例。
 调用 servlet 的 service 方法，同时传递 `ServletRequest `和 `ServletResponse `对象。
- 当 servlet 类被关闭的时候，调用 servlet 的 destroy 方法并卸载 servlet 类。
本章的第一个 servlet 容器不是全功能的。因此，她不能运行什么除了非常简单的 servlet，
而且也不调用 servlet 的 init 方法和 destroy 方法。相反它做了下面的事情：
- 等待 HTTP 请求。 构造一个` ServletRequest `对象和一个 `ServletResponse `对象。
- 假如该请求需要一个静态资源的话，调用 `StaticResourceProcessor `实例的 process 方
法，同时传递` ServletRequest `和 `ServletResponse `对象。
- 假如该请求需要一个 servlet 的话，加载 servlet 类并调用 servlet 的 service 方法，
同时传递` ServletRequest `和 `ServletResponse` 对象。

第一个 servlet 容器不是全功能的。因此，她不能运行什么除了非常简单的 servlet，
而且也不调用 servlet 的` init `方法和 destroy 方法。相反它做了下面的事情：
- 等待 HTTP 请求。 构造一个` ServletRequest `对象和一个 `ServletResponse `对象。
- 假如该请求需要一个静态资源的话，调用 `StaticResourceProcessor` 实例的 process 方
法，同时传递` ServletRequest `和 `ServletResponse `对象。
- 假如该请求需要一个 servlet 的话，加载 servlet 类并调用 servlet 的 service 方法，
同时传递` ServletRequest` 和 `ServletResponse `对象。
注意：在这个 servlet 容器中，每一次 servlet 被请求的时候， servlet 类都会被加载。

http://machineName:port/staticResource 可以请求一个静态资源。
为了请求一个 servlet，你可以使用下面的 URL:
http://machineName:port/servlet/servletClass
因此，假如你在本地请求一个名为 `PrimitiveServlet `的 servlet，你在浏览器的地址栏或
者网址框中敲入：
http://localhost:8080/servlet/PrimitiveServlet
servlet 容器可以就提供 PrimitiveServlet 了。不过，假如你调用其他 servlet，如
ModernServlet， servlet 容器将会抛出一个异常。
