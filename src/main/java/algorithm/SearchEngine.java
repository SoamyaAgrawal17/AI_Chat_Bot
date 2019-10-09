package algorithm;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.process.CoreLabelTokenFactory;
import edu.stanford.nlp.process.PTBTokenizer;
import lombok.extern.slf4j.Slf4j;
import opennlp.tools.stemmer.PorterStemmer;
import org.apache.commons.codec.language.Metaphone;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.awt.*;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.TreeMap;

@Slf4j
public class SearchEngine {

    private TreeMap<String, Integer> initialProcessing(String query) {
        TreeMap<String, Integer> queryMap = new TreeMap<>();
        //Query tokenization
        PTBTokenizer<CoreLabel> ptbtQuery = new PTBTokenizer<>(new StringReader(query), new CoreLabelTokenFactory(), "");

        while (ptbtQuery.hasNext()) {
            CoreLabel queryToken = ptbtQuery.next();
            // Query Stemming begins
            PorterStemmer s = new PorterStemmer();
            String querystring = queryToken.toString();
            querystring = querystring.toLowerCase();
            for (int c = 0; c < querystring.length(); c++) {
                s.add(querystring.charAt(c));
            }
            s.stem();
            String queryTerm;
            queryTerm = s.toString();

            if (queryTerm.matches("[a-zA-Z][a-z]+")) {

                // Query Metaphone begins
                Metaphone metaphone = new Metaphone();
                queryTerm = metaphone.encode(queryTerm);
            }
            Integer freq = queryMap.get(queryTerm);
            queryMap.put(queryTerm, (freq == null) ? 1 : freq + 1);
        }
        return queryMap;
    }

    private String[][] getKnowledgeBase(String fileName) throws IOException {

        KnowledgeBase database = new ObjectMapper().readValue(getClass().getResourceAsStream(fileName) , KnowledgeBase.class);
        int numberOfDocs = database.getDatasetList().size();
        String[][] knowledgeBase = new String[numberOfDocs][];

        int i = 0;
        for(Map<String, List<String>> data : database.getDatasetList()) {
            for (Map.Entry<String, List<String>> entry : data.entrySet()) {
                String question = entry.getKey();
                List<String> answers = entry.getValue();
                knowledgeBase[i] = new String[answers.size()+1];
                knowledgeBase[i][0] = question;
                int j = 1;
                for(String answer : answers) {
                    knowledgeBase[i][j] = answer;
                    j++;
                }
            }
            i++;
        }
        return knowledgeBase;
    }

    String searchResult(String query, String fileName) throws IOException, NoSuchAlgorithmException {

        String[][] knowledgeBase = getKnowledgeBase(fileName);

        HashMap<String, Double> sortedMapDesc = runSearchAlgorithm(query, knowledgeBase);
        Map.Entry<String, Double> entry = sortedMapDesc.entrySet().iterator().next();
        String key = entry.getKey();
        Double d = entry.getValue();

        if (d > 45) {
            key = key.substring(4);
            int minimum = 1;
            int maximum = knowledgeBase[Integer.parseInt(key) - 1].length - 1;
            Random r = SecureRandom.getInstanceStrong();
            int randomNumber = minimum + r.nextInt(maximum);
            return (knowledgeBase[Integer.parseInt(key) - 1][randomNumber] + "\n");
        } else {
            //code of search
            searchOnline(query);
            return "";

        }
    }

    private HashMap<String, Double> runSearchAlgorithm(String query, String[][] knowledgeBase) {
        // TreeMap 'queryMap'for storing keys(query terms) and value(query term
        // frequency).
        TreeMap<String, Integer> queryMap = initialProcessing(query);

        // Corpus-retrieving of documents


        // 'FinalTermFrequencyMap' is the TreeMap that displays the final document with dictionary
        // terms as tokens and integer value as document frequency
        TreeMap<String, Integer> finalTermFrequencyMap = new TreeMap<>();

        // Making an array list of all the individual Treemaps that represent
        // individual documents (in terms of tokens and term frequency).
        ArrayList<TreeMap<String, Integer>> list = new ArrayList<>();
        for (String[] aKnowledgeBase : knowledgeBase) {

            String requestData;

            requestData = aKnowledgeBase[0];

            //Question Tokenization begins
            TreeMap<String, Integer> individualTermFrequency = initialProcessing(requestData);

            for (Map.Entry<String, Integer> entry : individualTermFrequency.entrySet()) {
                String key = entry.getKey();
                Integer freq = finalTermFrequencyMap.get(key);
                finalTermFrequencyMap.put(key, (freq == null) ? 1 : freq + 1);
            }

            list.add(individualTermFrequency);
        }
        //Total Number of Documents-'totalDocuments'
        int totalDocuments = list.size();
        TreeMap<String, Double> rankedProduct = new TreeMap<>();

        for (Map.Entry<String, Integer> entry : finalTermFrequencyMap.entrySet()) {

            String key = entry.getKey();
            Integer documentFrequency = entry.getValue();
            Double rankedValue = (totalDocuments - documentFrequency + 0.5) / (documentFrequency + 0.5);
            rankedProduct.put(key, rankedValue);
        }


        // Making a HashMap that contains dictionary tokens and their final
        // product value which would be used to keep ranking of documents
        HashMap<String, Double> unsortMap = new HashMap<>();
        int i = 1;
        for (TreeMap<String, Integer> d : list) {
            Double product = 1.00;
            for (Map.Entry<String, Integer> entry : queryMap.entrySet()) {

                String key = entry.getKey();
                if (d.containsKey(key)) {
                    product = product * (rankedProduct.get(key));

                }
            }
            unsortMap.put("Doc " + i, product);
            i++;
        }
        // Making a new HashMap that would sort the HashMap that contained key
        // and unsorted product ranks in descending order
        return Util.sortByComparator(unsortMap, false);

    }

    private void searchOnline(String query) throws IOException {
        String someTerm = query.replaceAll(" ", "+");
        Scanner scanner = new Scanner(System.in);
        log.info("Enter 1 for information and 2 for location \n");
        int info = scanner.nextInt();
        String searchURL = "";
        if (info == 1)
            searchURL = "https://www.google.co.in/search" + "?q=" + someTerm + "&num=" + 5;
        if (info == 2) {
            searchURL = "https://www.google.co.in/maps" + "?q=" + someTerm + "&num=" + 5;
        }

        log.info(searchURL + "---------this is search url\n");

        try {
            Desktop desktop = java.awt.Desktop.getDesktop();
            URI oURL = new URI(searchURL);
            desktop.browse(oURL);
        } catch (Exception e) {
            log.error(e.getMessage());
        }


        //without proper User-Agent, we will get 403 error
        Document doc = Jsoup.connect(searchURL).userAgent("Mozilla/5.0").get();

        //If google search results HTML change the <h3 class="r" to <h3 class="r1"
        //we need to change below accordingly
        Elements results = doc.select("h3.r > a");

        for (Element result : results) {
            String linkHref = result.attr("href");
            String linkText = result.text();
            log.info("Text::" + linkText + ", URL::" + linkHref.substring(6, linkHref.indexOf('&')) + "\n");
        }
    }


}
