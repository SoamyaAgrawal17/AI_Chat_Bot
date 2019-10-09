package algorithm;

import lombok.extern.slf4j.Slf4j;

import java.util.Scanner;

@Slf4j
public class AIChatBot {

    public static void main(String[] args) throws Exception {

        System.out.println("HEY! Ask me something.");
        Scanner sc = new Scanner(System.in);
        while (true) {
            String query = sc.nextLine();
            if (query.equals("End the Chat.")) {
                break;
            }

            SearchEngine searchEngine = new SearchEngine();
            System.out.println(searchEngine.searchResult(query));
        }

        sc.close();
    }
}
