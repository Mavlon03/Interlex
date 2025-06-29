package uz.pdp.interlex.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uz.pdp.interlex.entity.ContactMessage;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContactMessageDto {

    @NotBlank(message = "Ism kiritilishi shart")
    @Size(max = 100, message = "Ism 100 ta belgidan oshmasligi kerak")
    private String name;

    @NotBlank(message = "Email kiritilishi shart")
    @Email(message = "Email formati noto'g'ri")
    @Size(max = 100, message = "Email 100 ta belgidan oshmasligi kerak")
    private String email;

    @NotBlank(message = "Telefon raqami kiritilishi shart")
    @Size(max = 20, message = "Telefon raqami 20 ta belgidan oshmasligi kerak")
    private String phone;

    private String serviceType;
    private String clientType;

    @NotBlank(message = "Mavzu kiritilishi shart")
    @Size(max = 200, message = "Mavzu 200 ta belgidan oshmasligi kerak")
    private String subject;

    @NotBlank(message = "Xabar matni kiritilishi shart")
    @Size(max = 2000, message = "Xabar matni 2000 ta belgidan oshmasligi kerak")
    private String message;

    private String langCode;

    public ContactMessage.ServiceType getServiceTypeEnum() {
        if (serviceType == null || serviceType.isEmpty()) {
            return null;
        }
        return switch (serviceType.toLowerCase()) {
            case "corporate" -> ContactMessage.ServiceType.CORPORATE;
            case "civil" -> ContactMessage.ServiceType.CIVIL;
            case "criminal" -> ContactMessage.ServiceType.CRIMINAL;
            case "family" -> ContactMessage.ServiceType.FAMILY;
            case "business" -> ContactMessage.ServiceType.BUSINESS;
            case "tax" -> ContactMessage.ServiceType.TAX;
            case "international" -> ContactMessage.ServiceType.INTERNATIONAL;
            default -> null;
        };
    }

    public ContactMessage.ClientType getClientTypeEnum() {
        if (clientType == null || clientType.isEmpty()) {
            return null;
        }
        return switch (clientType.toLowerCase()) {
            case "individual" -> ContactMessage.ClientType.INDIVIDUAL;
            case "legal" -> ContactMessage.ClientType.LEGAL;
            default -> null;
        };
    }
}
