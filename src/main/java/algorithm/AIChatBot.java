package algorithm;

import lombok.extern.slf4j.Slf4j;

import java.util.Scanner;

@Slf4j
public class AIChatBot {

    //Adding a comment
    public static void main(String[] args) throws Exception {

        log.info("HEY! Ask me something.");
        Scanner sc = new Scanner(System.in);
        while (true) {
            String query = sc.nextLine();
            if (query.equals("End the Chat.")) {
                break;
            }

            SearchEngine searchEngine = new SearchEngine();
            log.info(searchEngine.searchResult(query, "/corpus/knowledgeBase.json"));
        }

        sc.close();
    }
}
