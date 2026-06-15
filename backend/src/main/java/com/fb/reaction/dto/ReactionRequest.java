package com.fb.reaction.dto;

import com.fb.common.enums.ReactionType;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReactionRequest {

    @NotNull(message = "Reaction type is required")
    private ReactionType type;
}
