package org.acme.battle;

import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;
import org.acme.battle.model.Battle;
import org.jboss.resteasy.reactive.RestQuery;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Path("/battle")
public class BattleResource {


//    NOT REACTIVE CODE
//    @GET
//    public List<Battle> list() {
//        return Battle.listAll();
//    }
//
//    @GET
//    @Path("/user")
//    public List<Battle> listUserBattles(@RestQuery String userId, @RestQuery Boolean won) {
//        return Battle.findByUserAndWon(userId, won);
//    }
//
//    @GET
//    @Path("/count")
//    public Integer count(@RestQuery String userId, @RestQuery Boolean won) {
//        return Battle.findByUserAndWon(userId, won).size();
//    }
//    @GET
//    @Path("rank")
//    public Integer rank(@RestQuery String userId) {
//
//        // carica tutte le battaglie
//        List<Battle> battles = Battle.listAll();
//
//        // mappa utente -> battaglie raggruppando per userId
//        Map<String, List<Battle>> battlesByUser = battles.stream()
//                .collect(Collectors.groupingBy(battle -> battle.userId));
//
//        // crea una mappa utente -> percentuale di vittorie
//        Map<String, Double> userWinPercentageMap = battlesByUser.entrySet().stream()
//                .collect(Collectors.toMap(
//                        entry -> entry.getKey(),
//                        entry -> {
//                            List<Battle> userBattles = entry.getValue();
//                            long wonCount = userBattles.stream().filter(battle -> Boolean.TRUE.equals(battle.won)).count();
//                            long totalBattles = userBattles.size();
//                            return totalBattles > 0 ? (double) wonCount / totalBattles : 0.0;  // Calculate win percentage
//                        }
//                ));
//
//        // Ordina gli utenti per percentuale di vincite
//        List<String> rankedUsers = userWinPercentageMap.entrySet().stream()
//                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
//                .map(Map.Entry::getKey)
//                .collect(Collectors.toList());
//
//        // prende la posizione in classifica per l'utente
//        int rank = rankedUsers.indexOf(userId) + 1;
//
//        return rank;
//    }

    /**
     * Retrieves a reactive list of all battles.
     *
     * @return a Uni containing the list of all battles.
     */
    @GET
    public Uni<List<Battle>> list() {
        // Return all battles reactively
        return Battle.listAll();
    }

    /**
     * Retrieves a list of battles associated with a specific user, optionally filtered by whether the battles were won.
     *
     * @param userId the unique identifier of the user whose battles are to be retrieved
     * @param won    an optional filter to retrieve battles based on their win status;
     *               if true only won battles are retrieved,
     *               if false only lost battles are retrieved,
     *               and if null no filtering by won status is applied
     * @return a Uni emitting a list of Battle objects matching the specified criteria
     */
    @GET
    @Path("/user")
    public Uni<List<Battle>> listUserBattles(@QueryParam("userId") String userId,
                                             @QueryParam("won") Boolean won) {
        // Return battles for a specific user and won status reactively
        return Battle.findByUserAndWon(userId, won);
    }

    /**
     * Counts the number of battles for a specific user based on their unique identifier and the battle's win status.
     *
     * @param userId the unique identifier of the user whose battles are being counted; should not be null or empty.
     * @param won    a boolean indicating the win status of the battles to be counted; true for won battles, false for lost battles.
     * @return a Uni containing the number of battles that match the given user ID and win status.
     */
    @GET
    @Path("/count")
    public Uni<Integer> count(@QueryParam("userId") String userId,
                              @QueryParam("won") Boolean won) {
        // Count battles for a specific user and won status reactively
        return Battle.findByUserAndWon(userId, won)
                .onItem().transform(List::size); // Transform Uni<List<Battle>> to Uni<Integer>
    }

    /**
     * Retrieves the rank of a user based on their win percentage in battles.
     * The rank is determined by calculating and sorting win percentages of all users.
     *
     * @param userId the identifier of the user whose rank is to be determined
     * @return a {@link Uni} containing a {@link Response} with the user's rank as an integer if successful,
     * or an error message in case of failure
     */
    @GET
    @Path("rank")
    public Uni<Response> rank(@RestQuery String userId) {
        // Load all battles reactively
        return Battle.listAll()
                .onItem().transform(battles -> {

                    List<Battle> b = battles.stream()
                            .filter(entity -> entity instanceof Battle) // Ensure the instance is of type Battle
                            .map(entity -> (Battle) entity) // Cast to Battle
                            .collect(Collectors.toList());

                    // Map user -> battles grouping by userId
                    Map<String, List<Battle>> battlesByUser = b.stream()
                            .collect(Collectors.groupingBy(battle -> battle.userId));

                    // Create a map user -> win percentage
                    Map<String, Double> userWinPercentageMap = battlesByUser.entrySet().stream()
                            .collect(Collectors.toMap(
                                    entry -> entry.getKey(),
                                    entry -> {
                                        List<Battle> userBattles = entry.getValue();
                                        long wonCount = userBattles.stream().filter(battle -> Boolean.TRUE.equals(battle.won)).count();
                                        long totalBattles = userBattles.size();
                                        return totalBattles > 0 ? (double) wonCount / totalBattles : 0.0;  // Calculate win percentage
                                    }
                            ));

                    // Sort users by win percentage
                    List<String> rankedUsers = userWinPercentageMap.entrySet().stream()
                            .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                            .map(Map.Entry::getKey)
                            .collect(Collectors.toList());

                    // Get the rank position for the user
                    return rankedUsers.indexOf(userId) + 1; // This will return the user's rank
                })
                .onItem().transform(rank -> Response.ok(rank).build()) // Wrap rank in a Response
                .onFailure().recoverWithItem(failure -> Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .entity("Error occurred: " + failure.getMessage()).build()); // Handle errors
    }

}
