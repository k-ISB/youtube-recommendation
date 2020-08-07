/*
 * Copyright (c) 2012 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

//package com.google.api.services.samples.youtube.cmdline.youtube_cmdline_search_sample;

import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.List;

import java.util.Arrays;
import java.util.Comparator;

/**
 * Prints a list of videos based on a search term.
 *
 * @author Jeremy Walker
 */
public class Search {

    /** Global instance properties filename. */
    private static String PROPERTIES_FILENAME = "youtube.properties";

    /** Global instance of the HTTP transport. */
    private static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();

    /** Global instance of the JSON factory. */
    private static final JsonFactory JSON_FACTORY = new JacksonFactory();

    /** Global instance of the max number of videos we want returned (50 = upper limit per page). */
    private static final long NUMBER_OF_VIDEOS_RETURNED = 50;

    /** Global instance of Youtube object to make all API requests. */
    private static YouTube youtube;
    private static String apiKey = "AIzaSyBXQOaGVtBkLApM1XZK31Kzjkl1gXpy0fc";

    public static Movie[] movies = new Movie[50];
    private static int movieCounter = 0;

    /**
     * Initializes YouTube object to search for videos on YouTube (Youtube.Search.List). The program
     * then prints the names and thumbnails of each of the videos (only first 50 videos).
     *
     * @param args command line args.
     */
    public static void main(String[] args) {

        try {

            youtube = new YouTube.Builder(HTTP_TRANSPORT, JSON_FACTORY, new HttpRequestInitializer() {
                public void initialize(HttpRequest request) throws IOException {}
            }).setApplicationName("youtube-cmdline-search-sample").build();

            // Get query term from user.
            String queryTerm = getInputQuery();

            YouTube.Search.List search = youtube.search().list("id, snippet");


            search.setKey(apiKey);
            search.setQ(queryTerm);
            search.setOrder("viewCount");

            /*
             * We are only searching for videos (not playlists or channels). If we were searching for
             * more, we would add them as a string like this: "video,playlist,channel".
             */
            search.setType("video");
            /*
             * This method reduces the info returned to only the fields we need and makes calls more
             * efficient.
             */
            search.setFields("items(id/kind,id/videoId,snippet/title,snippet/thumbnails/default/url)");
            search.setMaxResults(NUMBER_OF_VIDEOS_RETURNED);
            SearchListResponse searchResponse = search.execute();

            List<SearchResult> searchResultList = searchResponse.getItems();

            if (searchResultList != null) {
                prettyPrint(searchResultList.iterator(), queryTerm);
            }
        } catch (GoogleJsonResponseException e) {
            System.err.println("There was a service error: " + e.getDetails().getCode() + " : "
                    + e.getDetails().getMessage());
        } catch (IOException e) {
            System.err.println("There was an IO error: " + e.getCause() + " : " + e.getMessage());
        } catch (Throwable t) {
            t.printStackTrace();
        }

        Arrays.sort(movies,Comparator.comparing(Movie::getLikeCount).reversed());
        Movie movie;
        for(int i=0; i<10; i++){
            movie = movies[i];
            System.out.println((i+1) + "つ目\n");
            System.out.println(" 動画タイトル: " + movie.getTitle());
            System.out.println(" 再生回数:" + movie.getViewCount());
            System.out.println(" 高評価数: " + movie.getLikeCount());
            System.out.println(" 動画url: https://www.youtube.com/watch?v=" + movie.getVideoId());
            System.out.println("\n-------------------------------------------------------------\n");
        }
    }

    /*
     * Returns a query term (String) from user via the terminal.
     */
    private static String getInputQuery() throws IOException {

        String inputQuery = "";

        System.out.print("検索キーワードを入力: ");
        BufferedReader bReader = new BufferedReader(new InputStreamReader(System.in));
        inputQuery = bReader.readLine();

        if (inputQuery.length() < 1) {
            // If nothing is entered, defaults to "YouTube Developers Live."
            inputQuery = "YouTube Developers Live";
        }
        return inputQuery;
    }

    private static void prettyPrint(Iterator<SearchResult> iteratorSearchResults, String query)throws Exception {

        String videoId;
        int likeCount;
        int viewCount;
        String title;

        System.out.println("\n=============================================================");
        System.out.println(
                " \"" + query + " \"" + "で検索します");
        System.out.println("=============================================================\n");

        if (!iteratorSearchResults.hasNext()) {
            System.out.println(" There aren't any results for your query.");
        }

        while (iteratorSearchResults.hasNext()) {

            SearchResult singleVideo = iteratorSearchResults.next();
            ResourceId rId = singleVideo.getId();

            // Double checks the kind is video.
            if (rId.getKind().equals("youtube#video")) {

                YouTube.Videos.List video = youtube.videos().list("id", "statistics");
                video.setPart("statistics");
                video.setId(rId.getVideoId());
                video.setKey(apiKey);
                VideoListResponse response = video.execute();

                List<Video> videoList = response.getItems();
                if (videoList.get(0).getStatistics().getLikeCount() != null) {

                    //int型の視聴回数と高評価数を取得
                    videoId = rId.getVideoId();
                    likeCount = Integer.parseInt(String.valueOf(videoList.get(0).getStatistics().getLikeCount()));
                    viewCount = Integer.parseInt(String.valueOf(videoList.get(0).getStatistics().getViewCount()));
                    title = singleVideo.getSnippet().getTitle();

                    movies[movieCounter] = new Movie(videoId, likeCount, viewCount, title);
                    if(movieCounter % 5 == 0) System.out.println("読み込み中(" + (int)(movieCounter/5) + "/10)");

                }else{
                    movies[movieCounter] = new Movie("", 0, 0, "");
                }
                movieCounter++;
            }
        }
    }
}