package com.zwp.usercenter.model.domain.request;

import lombok.Data;

import java.io.Serializable;

/**
 *  用户注册请求体
 */
@Data
public class UserRegisterRequest implements Serializable {
    // Java 序列化是一种将 对象的状态 转换为 字节流 的机制。这个字节流可以被保存到文件、通过网络传输，或者存储在数据库中。
    // 反序列化 则是序列化的逆过程，将字节流转换回原始的对象状态。
    // 序列化主要用于 持久化对象 和 远程方法调用 (RMI) 等场景

    private static final long serialVersionUID = 7458055790657432188L;
    // serialVersionUID 是一个 序列化版本号 (Serialization Version UID) 。 它的主要作用是 在 Java 序列化和反序列化过程中，用于验证类的版本兼容性

    private String userAccount;

    private String userPassword;

    private String checkPassword;

    private String planetCode;
}
