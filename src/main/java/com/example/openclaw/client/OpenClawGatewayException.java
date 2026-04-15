package com.example.openclaw.client;

public class OpenClawGatewayException extends RuntimeException {

    private final int upstreamStatus;
    private final String upstreamPath;
    private final String upstreamBody;

    public OpenClawGatewayException(int upstreamStatus, String upstreamPath, String upstreamBody) {
        super(buildMessage(upstreamStatus, upstreamPath, upstreamBody));
        this.upstreamStatus = upstreamStatus;
        this.upstreamPath = upstreamPath;
        this.upstreamBody = upstreamBody;
    }

    public int getUpstreamStatus() {
        return upstreamStatus;
    }

    public String getUpstreamPath() {
        return upstreamPath;
    }

    public String getUpstreamBody() {
        return upstreamBody;
    }

    private static String buildMessage(int upstreamStatus, String upstreamPath, String upstreamBody) {
        String body = upstreamBody == null || upstreamBody.isBlank() ? "<empty>" : upstreamBody;
        return "OpenClaw upstream request failed: POST " + upstreamPath
                + " returned " + upstreamStatus + " with body " + body;
    }
}
