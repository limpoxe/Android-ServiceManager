# Android-ServiceManager
简化android跨进程调用，无需aidl、service、messenger等，只需定义任意接口，并实现接口即可。


利用此框架可以轻松实现跨进程调用。

使用方法：

1、项目中引入libray工程, 另请关注libray工程manifest中的配置信息

    dependencies {
        compile project(":library")
    }

2、项目 Application 对象中的oncreate中初始化ServiceManager

    public void onCreate() {
        super.onCreate();

        //初始化manager
        ServiceManager.init(this);
    }

3、定义自己的业务接口和实现，例如：

    public interface A {
        boolean login(String username, String password);
    };
    
    //////////////
    public class AImpl implements A {

        public boolean login(String username, String password) {
            Log.d("login", username + ":" + password);
            return true;
        }

    }
    
4、在任意进程中注册服务：

    //第二个参数为接口实现类的类全名
    ServiceManager.publishService("a_service", AImpl.class.getName());
    
5、在任意进程中获取服务，实现跨进程调用

    A aservice = (A)ServiceManager.getService("a_service");
    aservice.login("hello", "world");
    
    
# 原理简介
    1、整个框架中分3个角色，对应最复杂的情况时的3个进程：
       调用方所在进程、服务管理器所在进程、服务提供方所在进程。
       这些进程可能是同一个进程，也可能是单独的进程。所有实际可能共有1个进程、2个进程、或者3个进程。
    
    2、注册binder
        在每个进程启动时，通过调用init方法，向服务管理器发布一个binder。用于实现进程间通信。
        
    3、发布服务
        服务提供方发布一个服务、做2件事：
        a）发布服务 实例 到当前进程（服务提供方所在进程）
        b）发布服务 描述 到服务管理器所在进程
        
    4、查找服务
        调用方进程查找服务的步骤如下：
        a）先在当前进程中查询服务实例
        b) 未找到，向服务管理器进程查询是否存在此对服务的描述
        c) 存在，调用方进程创建一个此服务的代理对象（在调用方进程中），并缓存到自己的进程中。
        d）调用方进程询问服务管理器进程 此服务的提供方所在的进程就在 服务管理器所在进程 or 另外一个进程中
        e) 若在服务管理器所在进程：此后所有由调用方进程发起的接口调用，全部路由到服务管理器进程，
                再由服务管理器进程在自己的进程中查询并调用服务接口，并将结果返回给调用方进程。
        f）若在其他进程：（表示此时对应的是最复杂的情况，涉及到3个进程）
                向服务管理器查询服务提供方所在进程的binder。即之前init时产生的binder
                并将此binder发送给调用方
                此后所有由调用方进程发起的接口调用，全部路由到这个binder，也即服务提供方所在进程的binder，由这
                个binder在自己的进程中查找并调用服务接口，并将接口返回给调用方进程。
                
    5、服务管理器实际是一个contentprovider。
        
                
                
        
        
