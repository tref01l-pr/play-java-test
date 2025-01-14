package Contracts.Requests;

import javax.validation.constraints.*;

public class FileMetadataRequest {
    @NotBlank(message = "File name must not be blank")
    @Size(max = 255, message = "File name must not exceed 255 characters")
    private String fileName;

    @NotBlank(message = "File type must not be blank")
    private String fileType;

    @NotBlank(message = "PDF hash must not be blank")
    private String pdfHash;

    @Min(value = 1, message = "Page count must be at least 1")
    @Max(value = 10000, message = "Page count must not exceed 10000")
    private int pageCount;

    public FileMetadataRequest(String fileName, String fileType, String pdfHash, int pageCount) {
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

    public String getHash() {
        return pdfHash;
    }

    public int getPageCount() {
        return pageCount;
    }
}
