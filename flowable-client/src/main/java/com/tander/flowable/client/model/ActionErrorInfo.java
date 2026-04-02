package com.tander.flowable.client.model;


import com.tander.flowable.client.action.BaseAction;

public record ActionErrorInfo(BaseAction action, Exception exception) {
}
