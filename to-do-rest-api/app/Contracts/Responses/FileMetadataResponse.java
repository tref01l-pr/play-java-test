package Contracts.Responses;

public class FileMetadataResponse {
    private String fileName;
    private String fileType;
    private String pdfHash;
    private int pageCount;

    public FileMetadataResponse(String fileName, String fileType, String pdfHash, int pageCount) {
        this.fileName = fileName;
        this.fileType = fileType;
        this.pdfHash = pdfHash;
        this.pageCount = pageCount;
    }

    public String getFileName() {
        return fileName;
    }

    public String getFileType() {
        return fileType;
    }

    public String getPdfHash() {
        return pdfHash;
    }

    public int getPageCount() {
        return pageCount;
    }
}
