package com.ipi.mesi_backend_rpg.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.ipi.mesi_backend_rpg.dto.ModuleAccessDTO;
import com.ipi.mesi_backend_rpg.dto.UserDTO;
import com.ipi.mesi_backend_rpg.mapper.ModuleAccessMapper;
import com.ipi.mesi_backend_rpg.model.AccessRight;
import com.ipi.mesi_backend_rpg.model.Module;
import com.ipi.mesi_backend_rpg.model.ModuleAccess;
import com.ipi.mesi_backend_rpg.model.NotificationType;
import com.ipi.mesi_backend_rpg.model.User;
import com.ipi.mesi_backend_rpg.repository.ModuleAccessRepository;
import com.ipi.mesi_backend_rpg.repository.ModuleRepository;
import com.ipi.mesi_backend_rpg.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ModuleAccessService {

    private final NotificationService notificationService;

    private final ModuleAccessRepository moduleAccessRepository;
    private final ModuleAccessMapper moduleAccessMapper;
    private final ModuleRepository moduleRepository;
    private final UserRepository userRepository;

    public ModuleAccessDTO getModuleAccessById(Integer id) {
        ModuleAccess moduleAccess = moduleAccessRepository.findById(id).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        return moduleAccessMapper.toDTO(moduleAccess);
    }

    public List<ModuleAccessDTO> getModuleAccessByModule(Module module) {
        List<ModuleAccess> moduleAccesses = moduleAccessRepository.findAllByModule(module);

        if (moduleAccesses == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        return moduleAccesses.stream().map(moduleAccessMapper::toDTO).toList();
    }

    public ModuleAccessDTO getModuleAccessByUser(Module module, User user) {
        ModuleAccess moduleAccess = moduleAccessRepository.findModuleAccessBymoduleAndUser(module, user);

        return moduleAccessMapper.toDTO(moduleAccess);
    }

    public ModuleAccessDTO createModuleAccess(Long moduleId, Long userId, Long user2Id) {

        ModuleAccess moduleAccess = new ModuleAccess();
        Module module = moduleRepository.findById(moduleId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        User user2 = userRepository.findById(user2Id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        moduleAccess.setModule(module);
        moduleAccess.setUser(user);
        moduleAccess.setCanEdit(true);
        moduleAccess.setCanView(true);
        moduleAccess.setCanPublish(true);
        moduleAccess.setCanInvite(true);
        moduleAccess = moduleAccessRepository.save(moduleAccess);

        // Ajout de la notification
        String content = user2.getUsername() + " a partagé le module \"" + module.getTitle() + "\" avec vous";
        notificationService.createNotification(
                NotificationType.MODULE_SHARED,
                content,
                user, // destinataire
                user2, // expéditeur
                module);

        return moduleAccessMapper.toDTO(moduleAccess);
    }

    public ModuleAccessDTO createModuleAccessFromDTO(ModuleAccessDTO accessDTO, Module module) {
        if (accessDTO == null) {
            throw new IllegalArgumentException("ModuleAccessDTO cannot be null for creation.");
        }
        if (module == null) {
            throw new IllegalArgumentException("Module cannot be null for creation.");
        }
        UserDTO userFromDto = accessDTO.user();
        if (userFromDto == null || userFromDto.id() == null) {
            throw new IllegalArgumentException(
                    "User information (UserDTO with ID) is missing in ModuleAccessDTO for creation.");
        }

        ModuleAccessDTO dtoForMapper = new ModuleAccessDTO(
                null,
                module.getId(),
                userFromDto,
                accessDTO.canView(),
                accessDTO.canEdit(),
                accessDTO.canPublish(),
                accessDTO.canInvite());

        ModuleAccess newAccessEntity = moduleAccessMapper.toEntity(dtoForMapper);

        ModuleAccess savedAccess = moduleAccessRepository.save(newAccessEntity);
        return moduleAccessMapper.toDTO(savedAccess);
    }

    public ModuleAccessDTO deleteModuleAccess(Integer id, Long userId) {
        ModuleAccess moduleAccess = moduleAccessRepository.findById(id).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND));
                
        Module module = moduleAccess.getModule();
        User user = moduleAccess.getUser();
        User actor = userRepository.findById(userId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        
        String content = String.format("Votre accès au module \"%s\" a été supprimé", module.getTitle());
        
        notificationService.createNotification(
            NotificationType.ACCESS_RIGHTS_REMOVED,
            content,
            user,  // destinataire
            actor, // expéditeur
            module
        );
        
        moduleAccessRepository.delete(moduleAccess);
        return moduleAccessMapper.toDTO(moduleAccess);
    }

    public ModuleAccessDTO toggleAccessRight(Integer id, AccessRight accessRight, Long userId) {
        ModuleAccess moduleAccess = moduleAccessRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "ModuleAccess not found with id: " + id));

        boolean oldValue = false;
        boolean newValue = false;
        String rightName = "";

        switch (accessRight) {
            case VIEW:
                oldValue = moduleAccess.isCanView();
                moduleAccess.setCanView(!oldValue);
                newValue = !oldValue;
                rightName = "visualisation";
                break;
            case EDIT:
                oldValue = moduleAccess.isCanEdit();
                moduleAccess.setCanEdit(!oldValue);
                newValue = !oldValue;
                rightName = "modification";
                break;
            case PUBLISH:
                oldValue = moduleAccess.isCanPublish();
                moduleAccess.setCanPublish(!oldValue);
                newValue = !oldValue;
                rightName = "publication";
                break;
            case INVITE:
                oldValue = moduleAccess.isCanInvite();
                moduleAccess.setCanInvite(!oldValue);
                newValue = !oldValue;
                rightName = "invitation";
                break;
            default:
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Invalid right type: " + accessRight + " only accept VIEW, EDIT, PUBLISH, INVITE");
        }

        // Créer une notification pour informer l'utilisateur du changement
        Module module = moduleAccess.getModule();
        User user = moduleAccess.getUser();

        // Utiliser SecurityUtils pour obtenir l'utilisateur actuel
        User actor = userRepository.findById(userId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (actor == null) {
            // Fallback si l'utilisateur n'est pas trouvé (e.g., opération système)
            actor = module.getCreator();
        }

        String action = newValue ? "accordé" : "retiré";
        String content = String.format("Le droit de %s pour le module \"%s\" vous a été %s",
                rightName, module.getTitle(), action);

        notificationService.createNotification(
                NotificationType.ACCESS_RIGHTS_CHANGED,
                content,
                user, // destinataire
                actor, // expéditeur (celui qui a modifié les droits)
                module);

        ModuleAccess savedAccess = moduleAccessRepository.save(moduleAccess);
        return moduleAccessMapper.toDTO(savedAccess);
    }

    public ModuleAccessDTO updateModuleAccess(Integer id, ModuleAccessDTO moduleAccessDTO, Long userId) {
        ModuleAccess moduleAccess = moduleAccessRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "ModuleAccess not found with id: " + id));

        // Sauvegarder les droits d'origine pour comparaison
        boolean oldCanView = moduleAccess.isCanView();
        boolean oldCanEdit = moduleAccess.isCanEdit();
        boolean oldCanPublish = moduleAccess.isCanPublish();
        boolean oldCanInvite = moduleAccess.isCanInvite();

        // Mettre à jour les droits
        moduleAccess.setCanView(moduleAccessDTO.canView());
        moduleAccess.setCanEdit(moduleAccessDTO.canEdit());
        moduleAccess.setCanPublish(moduleAccessDTO.canPublish());
        moduleAccess.setCanInvite(moduleAccessDTO.canInvite());

        // Si des droits ont changé, envoyer une notification
        if (oldCanView != moduleAccessDTO.canView() || 
            oldCanEdit != moduleAccessDTO.canEdit() || 
            oldCanPublish != moduleAccessDTO.canPublish() || 
            oldCanInvite != moduleAccessDTO.canInvite()) {
            
            Module module = moduleAccess.getModule();
            User user = moduleAccess.getUser();
            User actor = userRepository.findById(userId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
            
            StringBuilder changes = new StringBuilder();
            if (oldCanView != moduleAccessDTO.canView()) {
                changes.append(moduleAccessDTO.canView() ? "visualisation (ajouté)" : "visualisation (retiré)").append(", ");
            }
            if (oldCanEdit != moduleAccessDTO.canEdit()) {
                changes.append(moduleAccessDTO.canEdit() ? "modification (ajouté)" : "modification (retiré)").append(", ");
            }
            if (oldCanPublish != moduleAccessDTO.canPublish()) {
                changes.append(moduleAccessDTO.canPublish() ? "publication (ajouté)" : "publication (retiré)").append(", ");
            }
            if (oldCanInvite != moduleAccessDTO.canInvite()) {
                changes.append(moduleAccessDTO.canInvite() ? "invitation (ajouté)" : "invitation (retiré)").append(", ");
            }
            
            // Supprimer la dernière virgule et espace
            if (changes.length() > 2) {
                changes.delete(changes.length() - 2, changes.length());
            }
            
            String content = String.format("Vos droits d'accès au module \"%s\" ont été modifiés : %s", 
                    module.getTitle(), changes.toString());
            
            notificationService.createNotification(
                NotificationType.ACCESS_RIGHTS_CHANGED,
                content,
                user,  // destinataire
                actor, // expéditeur
                module
            );
        }

        ModuleAccess savedAccess = moduleAccessRepository.save(moduleAccess);
        return moduleAccessMapper.toDTO(savedAccess);
    }

    public void synchronizeModuleAccesses(Module module, List<ModuleAccessDTO> incomingAccessDTOs, Long userId) {
        if (module == null) {
            throw new IllegalArgumentException("Module cannot be null for synchronizing accesses.");
        }
        List<ModuleAccessDTO> dtosToProcess = (incomingAccessDTOs == null) ? new ArrayList<>() : incomingAccessDTOs;

        // Récupérer les accès actuels en BD pour ce module
        Map<Integer, ModuleAccess> currentDbAccessMap = moduleAccessRepository.findAllByModule(module).stream()
                .collect(Collectors.toMap(ModuleAccess::getId, Function.identity()));

        // Ensemble des IDs des DTOs entrants qui ont un ID non nul (pour identifier
        // les mises à jour et les suppressions)
        Set<Integer> incomingDtoNonNullIds = dtosToProcess.stream()
                .map(ModuleAccessDTO::id)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        // Utiliser une copie pour pouvoir y ajouter les IDs des accès qui sont "créés"
        // mais qui en fait mettaient à jour un existant
        Set<Integer> finalIdsToKeep = new HashSet<>(incomingDtoNonNullIds);

        // Traiter les créations et les mises à jour
        for (ModuleAccessDTO dto : dtosToProcess) {
            UserDTO userFromDto = dto.user();
            if (userFromDto == null || userFromDto.id() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "User information (UserDTO with ID) is required for each ModuleAccessDTO.");
            }

            if (dto.id() != null) { // Le DTO suggère une MISE À JOUR (car il a un ID)
                ModuleAccess existingAccessEntity = currentDbAccessMap.get(dto.id());
                if (existingAccessEntity != null) { // L'ID du DTO existe en BD
                    // Vérifications de cohérence
                    if (existingAccessEntity.getModule().getId() != module.getId()) {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                "ModuleAccess with ID " + dto.id() + " does not belong to the provided module "
                                        + module.getId() + ".");
                    }
                    if (!existingAccessEntity.getUser().getId().equals(userFromDto.id())) {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                "ModuleAccess with ID " + dto.id() + " is for user "
                                        + existingAccessEntity.getUser().getId() +
                                        ", but DTO specifies user " + userFromDto.id()
                                        + ". User cannot be changed on an existing access.");
                    }
                    updateModuleAccess(dto.id(), dto, userId); // Met à jour les droits
                } else {
                    // Le DTO a un ID, mais cet ID n'est pas (ou plus) dans la base de données pour
                    // ce module.
                    throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "ModuleAccess with ID " + dto.id() + " provided for update, but not found for module "
                                    + module.getId() + ".");
                }
            } else { // Le DTO suggère une CRÉATION (car id est null)
                User userEntityForDto = userRepository.findById(userFromDto.id())
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                "User with id " + userFromDto.id() + " specified in DTO not found."));

                ModuleAccess accessAlreadyExistsForUser = moduleAccessRepository.findModuleAccessBymoduleAndUser(module,
                        userEntityForDto);

                if (accessAlreadyExistsForUser != null) {
                    // Un accès existe déjà pour cette combinaison module/utilisateur.
                    // On le met à jour avec les droits du DTO courant.
                    ModuleAccessDTO dtoForExistingAccessUpdate = new ModuleAccessDTO(
                            accessAlreadyExistsForUser.getId(),
                            module.getId(),
                            userFromDto,
                            dto.canView(),
                            dto.canEdit(),
                            dto.canPublish(),
                            dto.canInvite());
                    updateModuleAccess(accessAlreadyExistsForUser.getId(), dtoForExistingAccessUpdate, userId);
                    finalIdsToKeep.add(accessAlreadyExistsForUser.getId()); // S'assurer que cet ID n'est pas supprimé
                } else {
                    // C'est une vraie nouvelle création.
                    ModuleAccessDTO createdAccessDTO = createModuleAccessFromDTO(dto, module);
                    finalIdsToKeep.add(createdAccessDTO.id()); // Ajouter l'ID nouvellement créé à la liste des IDs à
                                                               // conserver
                }
            }
        }

        // Traiter les SUPPRESSIONS
        Set<Integer> dbIdsToDelete = currentDbAccessMap.keySet().stream()
                .filter(dbId -> !finalIdsToKeep.contains(dbId))
                .collect(Collectors.toSet());

        for (Integer idToDelete : dbIdsToDelete) {
            deleteModuleAccess(idToDelete, userId);
        }
    }
}
