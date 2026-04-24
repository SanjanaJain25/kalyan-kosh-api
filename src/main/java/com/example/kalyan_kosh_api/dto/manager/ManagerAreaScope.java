package com.example.kalyan_kosh_api.dto.manager;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ManagerAreaScope {

    private boolean unrestricted;

    private List<String> sambhagIds;
    private List<String> districtIds;
    private List<String> blockIds;
}