package com.example.flood_alert.dbo.request;
import java.util.UUID;

import com.google.firebase.database.annotations.NotNull;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

//Province giao nhiệm vụ hỗ trợ cho team
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AssignSupportTeamItemRequest {
    @NotNull 
    UUID supportRequestItemId;

    @NotNull
    UUID teamId;
}
