package pl.dlusk.api.controller.rest;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.dlusk.api.dto.OwnerDTO;
import pl.dlusk.api.dto.OwnersDTO;
import pl.dlusk.api.dto.mapper.OwnerMapper;
import pl.dlusk.business.OwnerService;
import pl.dlusk.domain.Owner;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/owners")
@AllArgsConstructor
public class RestOwnerController {
    private final OwnerService ownerService;
    private final OwnerMapper ownerMapper;


    @GetMapping
    public OwnersDTO getOwnersList() {
        List<Owner> allOwners = ownerService.getAllOwners();
        List<OwnerDTO> allOwnersDTO = ownersToOwnersDTO(allOwners);
        OwnersDTO ownersDTO = OwnersDTO.of(allOwnersDTO);
        return ownersDTO;
    }

    private List<OwnerDTO> ownersToOwnersDTO(List<Owner> allOwners) {
        List<OwnerDTO> allOwnersDTO = new ArrayList<>();
        for (Owner allOwner : allOwners) {
            OwnerDTO ownerDTO = ownerMapper.mapToDTO(allOwner);
            allOwnersDTO.add(ownerDTO);
        }
        return allOwnersDTO;
    }

    @GetMapping("{ownerId}")
    public OwnerDTO getOwnerDetails(@PathVariable Long ownerId) {
        log.info("getClientDetails ### clientId {}", ownerId);
        Owner owner = ownerService.getOwnerById(ownerId);
        log.info("getClientDetails ### owner {}", owner);
        OwnerDTO ownerDTO = ownerMapper.mapToDTO(owner);
        log.info("getClientDetails ### ownerDTO {}", ownerDTO);
        return ownerDTO;
    }
}
