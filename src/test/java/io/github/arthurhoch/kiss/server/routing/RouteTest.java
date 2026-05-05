package io.github.arthurhoch.kiss.server.routing;

import io.github.arthurhoch.kiss.server.http.HttpMethod;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RouteTest {
    @Test
    void dynamicRoutesDoNotCollapseEmptyPathSegments() {
        Route route = Route.normal(HttpMethod.GET, "/users/{id}", context -> context.text("OK"));

        assertTrue(route.match("/users/123").isPresent());
        assertEquals("123", route.match("/users/123").orElseThrow().get("id"));
        assertFalse(route.match("/users//123").isPresent());
        assertFalse(route.match("/users/").isPresent());
    }

    @Test
    void exactRoutesAreMethodSpecific() {
        Router router = new Router();
        router.add(HttpMethod.GET, "/items", context -> context.text("GET"));
        router.add(HttpMethod.POST, "/items", context -> context.text("POST"));

        assertEquals(HttpMethod.GET, router.match(HttpMethod.GET, "/items").orElseThrow().route().method());
        assertEquals(HttpMethod.POST, router.match(HttpMethod.POST, "/items").orElseThrow().route().method());
        assertFalse(router.match(HttpMethod.DELETE, "/items").isPresent());
        assertEquals(EnumSet.of(HttpMethod.GET, HttpMethod.POST), router.allowedMethods("/items"));
    }

    @Test
    void dynamicRoutesExtractMultipleParams() {
        Route route = Route.normal(HttpMethod.GET, "/organizations/{organizationId}/locations/{locationId}",
                context -> context.text("OK"));

        assertEquals("acme", route.match("/organizations/acme/locations/paris").orElseThrow().get("organizationId"));
        assertEquals("paris", route.match("/organizations/acme/locations/paris").orElseThrow().get("locationId"));
    }

    @Test
    void exactRoutesHavePriorityOverDynamicRoutes() {
        Router router = new Router();
        router.add(HttpMethod.GET, "/users/{id}", context -> context.text("dynamic"));
        router.add(HttpMethod.GET, "/users/me", context -> context.text("exact"));

        assertFalse(router.match(HttpMethod.GET, "/users/me").orElseThrow().route().path().contains("{"));
    }

    @Test
    void queryStringDoesNotAffectRouterMatching() {
        Router router = new Router();
        router.add(HttpMethod.GET, "/users/{id}", context -> context.text("dynamic"));

        RouteMatch match = router.match(HttpMethod.GET, "/users/123?active=true").orElseThrow();

        assertEquals("123", match.pathParams().get("id"));
    }

    @Test
    void fastRoutesHavePriorityOverNormalRoutes() {
        Router router = new Router();
        router.add(HttpMethod.GET, "/health", context -> context.text("normal"));
        router.addDirect(HttpMethod.GET, "/health", () -> new byte[0]);

        assertTrue(router.match(HttpMethod.GET, "/health").orElseThrow().route().fast());
    }

    @Test
    void trailingSlashDoesNotMatchRouteWithoutTrailingSlash() {
        Router router = new Router();
        router.add(HttpMethod.GET, "/health", context -> context.text("OK"));

        assertTrue(router.match(HttpMethod.GET, "/health").isPresent());
        assertFalse(router.match(HttpMethod.GET, "/health/").isPresent());
    }

    @Test
    void rejectsInvalidRouteDefinitions() {
        assertThrows(NullPointerException.class, () -> Route.normal(HttpMethod.GET, "/x", null));
        assertThrows(IllegalArgumentException.class, () -> Route.normal(HttpMethod.GET, "x", context -> context.text("OK")));
        assertThrows(IllegalArgumentException.class, () -> new Router().addDirect(HttpMethod.GET, "/users/{id}", () -> new byte[0]));
    }
}
