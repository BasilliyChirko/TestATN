package basilliyc.chirkotestatn.entity;

public class StoredMediaInfo {
    private String fileName;
    private long fileLength;
    private String filePath;
    private long time;
    private boolean isReceived;

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

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public boolean isReceived() {
        return isReceived;
    }

    public void setReceived(boolean received) {
        isReceived = received;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    @Override
    public String toString() {
        return "StoredMediaInfo{" +
                "fileName='" + fileName + '\'' +
                ", fileLength=" + fileLength +
                ", filePath='" + filePath + '\'' +
                ", time=" + time +
                ", isReceived=" + isReceived +
                '}';
    }
}
