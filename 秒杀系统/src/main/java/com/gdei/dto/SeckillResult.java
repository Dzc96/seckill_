package com.gdei.dto;

/**
 * 封装所有ajax请求的返回类型，方便返回json，即封装的是json的数据
 * 即状态，数据，错误信息
 * @param <T>
 */
public class SeckillResult<T> {
    private boolean success;
    private T data;
    private String error;

    public SeckillResult() {
    }

    public SeckillResult(boolean success, T data) {
        this.success = success;
        this.data = data;
    }

    public SeckillResult(boolean success, String error) {
        this.success = success;
        this.error = error;
    }

    @Override
    public String toString() {
        return "SeckillResult{" +
                "状态=" + success +
                ", 数据=" + data +
                ", 错误信息='" + error + '\'' +
                '}';
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}
