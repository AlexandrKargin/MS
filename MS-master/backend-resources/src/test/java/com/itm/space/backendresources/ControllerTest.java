package com.itm.space.backendresources;


import com.itm.space.backendresources.api.request.UserRequest;
import com.itm.space.backendresources.exception.BackendResourcesException;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.RoleMappingResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.MappingsRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.mockito.Mock;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;


import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WithMockUser(username = "test_user", roles = "MODERATOR")
public class ControllerTest extends BaseIntegrationTest{

    @MockBean
    private Keycloak keycloak;
    @Mock
    private UserResource userResource;
    @Mock
    private UsersResource usersResource;
    @Mock
    private Response response;
    @Mock
    private RealmResource realmResource;
    @Mock
    private UserRepresentation userRepresentation;
    @Mock
    private RoleMappingResource roleMappingResource;
    @Mock
    private MappingsRepresentation mappingsRepresentation;

    @BeforeEach
    public void setUp() {
        when(keycloak.realm(anyString())).thenReturn(realmResource);
        when(realmResource.users()).thenReturn(usersResource);
    }

    private final UserRequest validUserRequest = new UserRequest("test1", "test@mail.ru",
            "test1234", "te", "st");

    @Test
    @SneakyThrows
    public void testCreateUserWithInvalidRequest() {

        UserRequest invalidUserRequest = new UserRequest("test", "testiest", "test1234", "te", "st");

        mvc.perform(requestWithContent(post("/api/users"), invalidUserRequest))
                .andExpect(status().is(HttpStatus.BAD_REQUEST.value()))
                .andDo(print());
    }

    @Test
    @SneakyThrows
    public void shouldCreateUserSuccessfully() {

        when(usersResource.create(any(UserRepresentation.class))).thenReturn(response);
        when(response.getStatusInfo()).thenReturn(Response.Status.CREATED);

        mvc.perform(requestWithContent(post("/api/users"), validUserRequest))
                .andExpect(status().is(HttpStatus.OK.value()))
                .andDo(print());
    }

    @Test
    public void testCreateUserThrowsException() throws Exception {

        when(usersResource.create(any(UserRepresentation.class))).thenThrow(new WebApplicationException("test exception"));

        mvc.perform(requestWithContent(post("/api/users"), validUserRequest))
                .andExpect(status().is(HttpStatus.INTERNAL_SERVER_ERROR.value()))
                .andExpect(content().string(containsString("test exception")))
                .andDo(print());
    }

    @Test
    @SneakyThrows
    public void testGetUserById() {
        String id = "903a8dd4-ebcc-49d9-9436-b8b6464d5d10";

        when(realmResource.users().get(eq(id))).thenReturn(userResource);
        when(userResource.toRepresentation()).thenReturn(userRepresentation);
        when(userResource.roles()).thenReturn(roleMappingResource);
        when(userResource.roles().getAll()).thenReturn(mappingsRepresentation);

        mvc.perform(MockMvcRequestBuilders.get("/api/users/{id}", id))
                .andExpect(status().is(HttpStatus.OK.value()))
                .andDo(print());
    }

    @Test
    public void testGetUserByIdThrowException() throws Exception {
        String id = "903a8dd4-ebcc-49d9-9436-b8b6464d5d10";

        when(realmResource.users().get(eq(id))).thenThrow(new BackendResourcesException("Error message", HttpStatus.INTERNAL_SERVER_ERROR));

        mvc.perform(MockMvcRequestBuilders.get("/api/users/{id}", id))
                .andExpect(status().is(HttpStatus.INTERNAL_SERVER_ERROR.value()))
                .andExpect(content().string(containsString("Error message")))
                .andDo(print());
    }

    @Test
    @SneakyThrows
    public void testHello() {

        mvc.perform(MockMvcRequestBuilders.get("/api/users/hello"))
                .andDo(print())
                .andExpect(status().is(HttpStatus.OK.value()))
                .andExpect(content().string(containsString("test_user")));
    }
}
