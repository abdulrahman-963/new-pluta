package com.pluta.camera.context;

import java.util.List;

public class TenantContext {

    private TenantContext(){}

    private static final ThreadLocal<TenantInfo> CONTEXT = new ThreadLocal<>();

    public static void setTenantInfo(Long tenantId, Long branchId ) {
        CONTEXT.set(new TenantInfo(tenantId, branchId));
    }

    public static TenantInfo getTenantInfo() {
        return CONTEXT.get();
    }

    public static Long getTenantId() {
        TenantInfo info = CONTEXT.get();
        return info != null ? info.tenantId() : null;
    }

    public static Long getBranchId() {
        TenantInfo info = CONTEXT.get();
        return info != null ? info.branchId() : null;
    }


    public static void clear() {
        CONTEXT.remove();
    }

    public record TenantInfo(Long tenantId, Long branchId){}

  /*  @Getter
    @Setter
    public static class TenantInfo {
        private String tenantCode;
        private String branchCode;

        public TenantInfo(String tenantCode, String branchCode) {
            this.tenantCode = tenantCode;
            this.branchCode = branchCode;
        }
    }*/
}