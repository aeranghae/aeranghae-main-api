package cloud.aeranghae.main.controller.dto;

/**
 * 📦 데이터를 실어 나르는 바구니 (DTO)
 * 엔티티를 직접 외부에 노출하지 않기 위해 사용하는 전달용 객체입니다.
 */
@lombok.Getter
@lombok.NoArgsConstructor
public class ProjectSaveRequestDto {
    private String title;
    private String description;
    private String repositoryUrl;
}