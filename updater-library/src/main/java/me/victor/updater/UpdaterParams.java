package me.victor.updater;

public class UpdaterParams {
    private boolean isForceUpdate;
    private String updateTitle;
    private String updateMsg;
    private String downloadUrl;
    private String authority;

    public boolean isForceUpdate() {
        return isForceUpdate;
    }

    public void setForceUpdate(boolean forceUpdate) {
        isForceUpdate = forceUpdate;
    }

    public String getUpdateTitle() {
        return updateTitle;
    }

    public void setUpdateTitle(String updateTitle) {
        this.updateTitle = updateTitle;
    }

    public String getUpdateMsg() {
        return updateMsg;
    }

    public void setUpdateMsg(String updateMsg) {
        this.updateMsg = updateMsg;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    public String getAuthority() {
        return authority;
    }

    public void setAuthority(String authority) {
        this.authority = authority;
    }

    public static class Builder {
        private boolean isForceUpdate;
        private String updateTitle;
        private String updateMsg;
        private String downloadUrl;
        private String authority;

        public Builder isForceUpdate(boolean isForceUpdate) {
            this.isForceUpdate = isForceUpdate;
            return this;
        }

        public Builder setUpdateTitle(String updateTitle) {
            this.updateTitle = updateTitle;
            return this;
        }

        public Builder setUpdateMsg(String updateMsg) {
            this.updateMsg = updateMsg;
            return this;
        }

        public Builder setDownloadUrl(String downloadUrl) {
            this.downloadUrl = downloadUrl;
            return this;
        }

        public Builder setAuthority(String authority) {
            this.authority = authority;
            return this;
        }

        public UpdaterParams build() {
            UpdaterParams params = new UpdaterParams();
            params.isForceUpdate = isForceUpdate;
            params.updateTitle = updateTitle;
            params.updateMsg = updateMsg;
            params.downloadUrl = downloadUrl;
            params.authority = authority;
            return params;
        }
    }
}
