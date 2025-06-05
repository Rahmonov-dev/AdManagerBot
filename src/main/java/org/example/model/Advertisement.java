package org.example.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class Advertisement {
    private Long id;
    private Long userId;
    private String productName;
    private String price;
    private String location;
    private String description;
    private String photoFileId;
    private String contactInfo;
    private LocalDateTime createdAt;
    private boolean isConfirmed;

    public Advertisement() {
        this.createdAt = LocalDateTime.now();
        this.isConfirmed = false;
    }

    public String toFormattedString() {
        return "\uD83D\uDD25 SUPER TAKLIF! \n\n" +
                "\uD83D\uDCE6 Mahsulot: " + (productName != null ? productName : "") + "\n\n" +
                "\uD83D\uDCB8 Narx: " + (price != null ? price : "") + "\n\n" +
                "\uD83D\uDCCC Joylashuv: " + (location != null ? location : "") + "\n\n" +
                "\uD83D\uDD0D Tavsif: " + (description != null ? description : "") + "\n\n" +
                (photoFileId != null && !photoFileId.isEmpty() ? "\uD83D\uDDBC\uFE0F Rasm: Mavjud\n\n" : "") +
                "\uD83D\uDCF2 Aloqa: " + (contactInfo != null ? contactInfo : "");
    }
}

