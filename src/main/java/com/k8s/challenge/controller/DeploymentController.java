package com.k8s.challenge.controller;

import com.k8s.challenge.dto.CreateDeploymentDto;
import com.k8s.challenge.resource.DeploymentResource;
import com.k8s.challenge.service.DeploymentService;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.models.V1Deployment;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.convert.ConversionService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping(value = "/api/v1/user/{userName}/deployment")
@AllArgsConstructor
public class DeploymentController {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeploymentController.class);
    private final DeploymentService deploymentService;
    private final ConversionService conversionService;

    @GetMapping
    @PreAuthorize(value = "hasRole('ROLE_USER')")
    public ResponseEntity<List<DeploymentResource>> listDeployments
            (@RequestParam(value = "namespace", required = false) String namespace,
             @PathVariable(value = "userName") String userName) throws ApiException {
        validateSecurityUserNameWithGivenUserName(userName);
        return ResponseEntity.ok(deploymentService.listDeployments(userName, namespace));
    }

    @PostMapping
    @PreAuthorize(value = "hasRole('ROLE_USER')")
    public ResponseEntity<DeploymentResource> createDeployment(@RequestBody @Valid CreateDeploymentDto createDeploymentDto,
                                                               @PathVariable(value = "userName") String userName) throws ApiException {
        validateSecurityUserNameWithGivenUserName(userName);
        return ResponseEntity.ok(deploymentService.createDeployment(userName, createDeploymentDto.getNamespace(),
                conversionService.convert(createDeploymentDto, V1Deployment.class), createDeploymentDto.getPretty(),
                createDeploymentDto.getDryRun()));
    }

    private void validateSecurityUserNameWithGivenUserName(String userName) {
        if (!SecurityContextHolder.getContext().getAuthentication().getPrincipal().equals(userName)) {
            LOGGER.warn("Token is not used for correct user operation. Token mismatched!. Token username : {}, Request username: {}",
                    SecurityContextHolder.getContext().getAuthentication().getPrincipal(), userName);
            throw new AccessDeniedException("You are not allowed to perform this operation. Wrong token!");
        }
    }
}
