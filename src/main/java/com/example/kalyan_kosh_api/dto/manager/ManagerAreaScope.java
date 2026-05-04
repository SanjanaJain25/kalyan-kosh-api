package com.example.kalyan_kosh_api.dto.manager;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ManagerAreaScope {

    private boolean unrestricted;

    private List<UUID> sambhagIds;
    private List<UUID> districtIds;
    private List<UUID> blockIds;
}