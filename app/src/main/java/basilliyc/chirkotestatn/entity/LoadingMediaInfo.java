package basilliyc.chirkotestatn.entity;

public class LoadingMediaInfo {
    private String fileName;
    private long fileLength;
    private long loadingLength;

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public long getFileLength() {
        return fileLength;
    }

    public void setFileLength(long fileLength) {
        this.fileLength = fileLength;
    }

    public long getLoadingLength() {
        return loadingLength;
    }

    public void setLoadingLength(long loadingLength) {
        this.loadingLength = loadingLength;
    }

    @Override
    public String toString() {
        return "LoadingMediaInfo{" +
                "fileName='" + fileName + '\'' +
                ", fileLength=" + fileLength +
                ", loadingLength=" + loadingLength +
                '}';
    }
}
