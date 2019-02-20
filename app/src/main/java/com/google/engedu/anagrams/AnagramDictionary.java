/* Copyright 2016 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.engedu.anagrams;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

public class AnagramDictionary {

    private static final int MIN_NUM_ANAGRAMS = 5;
    private static final int DEFAULT_WORD_LENGTH = 3;
    private static final int MAX_WORD_LENGTH = 7;
    private Random random = new Random();
    private int wordLength = DEFAULT_WORD_LENGTH;

    //Data set containing our words
    private ArrayList<String> wordList = new ArrayList<String>();

    //Milestone 1
    //A HashSet (called wordSet) that will allow us to rapidly (in O(1)) verify whether a word is valid.
    //A HashMap (called lettersToWord) that will allow us to group anagrams together.
    //Tbh I'm not really sure where to implement wordSet. After Milestone 3, the working compound object
    //(sizeToWords) is much more efficient simply because it doesn't have the entire dictionary of input words,
    //it only works with a small subset.
    private HashSet<String> wordSet = new HashSet<String>();
    private HashMap<String, ArrayList<String>> lettersToWord = new HashMap<String, ArrayList<String>>();
    private HashMap<Integer, ArrayList<String>> sizeToWords = new HashMap<Integer, ArrayList<String>>();

    public AnagramDictionary(Reader reader, int inputWordLength) throws IOException {
        BufferedReader in = new BufferedReader(reader);
        String line;
        wordLength = inputWordLength;
        while((line = in.readLine()) != null) {
            String word = line.trim();
            wordList.add(word);
            //If sizeToWords contains an array corresponding to the length of the current word,
            //Create a temp array that adds the new word, then replace the array for the key
            //with the new temp array.
            if(sizeToWords.containsKey(word.length())) {
                ArrayList<String> tempSizeArray = sizeToWords.get(word.length());
                tempSizeArray.add(word);
                sizeToWords.put(word.length(), tempSizeArray);
            }
            //If sizeToWords doesn't contain the key yet, make an array holding this single item
            //And push that array to a new zieToWords entry.
            else {
                ArrayList<String> tempSizeArray = new ArrayList<>();
                tempSizeArray.add(word);
                sizeToWords.put(word.length(), tempSizeArray);
            }
        }
    }

    //Milestone 2
    //Implement isGoodWord such that the provided word is a valid dictionary word
    //And that it is not a substring of the original base word.
    public boolean isGoodWord(String word, String base) {
        //Really the only thing we need to do is check to see if our HashMap lettersToWord
        //contains the user's input word as an anagram of the base word.
        //That base word, sorted by character, is the key we use for lookup in the
        //lettersToWord HashMap.
        if(lettersToWord.get(sortLetters(base)).contains(word)) {
            return true;
        }
        else {
            return false;
        }
    }


    //After MileStone 2, getAnagrams is redundant.
    //In Milestone 1 it's recommended to sort the base by characters
    //And then get all words that sort to that sorted String.
    //So POST complies with a base word of STOP.
    public List<String> getAnagrams(String targetWord) {
        ArrayList<String> result = new ArrayList<String>();
        String inputWord = sortLetters(targetWord);
        System.out.println(inputWord);
        for(int i = 0; i < wordList.size(); i++) {
            if(inputWord.equals(sortLetters(wordList.get(i)))) {
                result.add(wordList.get(i));
            }
        }
        return result;
    }

    //Creates a string of the sorted characters of a base word
    private String sortLetters(String word) {
        //First cast the base word to an array of chars.
        char[] chars = word.toCharArray();
        //Sort that array alphabetically.
        Arrays.sort(chars);
        //Store the new sorted string as the result
        String result = new String(chars);

        //Return the sorted string.
        return result;
    }

    public List<String> getAnagramsWithOneMoreLetter(String word) {
        ArrayList<String> result = new ArrayList<String>();

        if(lettersToWord.containsKey(sortLetters(word))) {
            result = lettersToWord.get(sortLetters(word));
        }
        else {
            ArrayList<String> firstPass = new ArrayList<String>();
            ArrayList<String> secondPass = new ArrayList<String>();

            char[] alphaChars = "abcdefghijklmnopqrstuvwxyz".toCharArray();
            for (int i = 0; i < alphaChars.length; i++) {
                //Make a StringBuilder object so that we can add a char to the base word
                StringBuilder currentLetterUpdate = new StringBuilder(word.length() + 1);
                //Set the base of the StringBuilder to the word that was input to this function
                currentLetterUpdate.append(word);
                //Add the current alpha char to the end of the StringBuilder
                currentLetterUpdate.append(alphaChars[i]);
                //Sort the String Builder and cast it to a string
                String addedLetter = sortLetters(currentLetterUpdate.toString());
                //Add the new string to the results.
                firstPass.add(addedLetter);
            }

            //With all of the strings of one letter added to the base word,
            //We now have to check if any dictionary words match up to these sorted strings.
            ArrayList<String> wordsWithSize = sizeToWords.get(word.length()+1);
            for (int j = 0; j < wordsWithSize.size(); j++) {
                if (firstPass.contains(sortLetters(wordsWithSize.get(j)))) {
                    secondPass.add(wordsWithSize.get(j));
                }
            }

            //Last, we'll need to check if the base word is a substring of the anagram.
            //If it is, it won't be included in our result ArrayList.
            for (int m = 0; m < secondPass.size(); m++) {
                if (secondPass.get(m).contains(word)) {
                    continue;
                } else {
                    result.add(secondPass.get(m));
                }
            }

            lettersToWord.put(sortLetters(word), result);
        }

        return result;
    }

    //Milestone 2
    //Implement pickGoodStarterWord such that it randomly selects a word
    //in our wordList that has at least MIN_NUM_ANAGRAMS number of anagrams.
    public String pickGoodStarterWord() {
        //The minimum number can be changed.
        //Index of word in the dictionary
        int wordIndex = 0;
        String compWord;
        String result;

        while(true) {
            wordIndex = random.nextInt(wordList.size());
            compWord = wordList.get(wordIndex);
            //Get all the anagrams of the word at the wordIndex
            //Make sure that the word complies with the wordLength input into the constructor
            //Then check to see if we can get the right number of anagrams corresponding to the
            //input value
            if (compWord.length() >= wordLength) {
                if(compWord.length() == wordLength) {
                    List<String> wordsWithSize = getAnagramsWithOneMoreLetter(compWord);
                    //If the number of anagrams with this word exceeds or equals the minimum
                    // we initially set, break the loop and set this word as our play word.
                    if (wordsWithSize.size() >= MIN_NUM_ANAGRAMS) {
                        result = compWord;
                        if(wordLength < MAX_WORD_LENGTH) { wordLength++; }
                        break;
                    }
                }
            }
            //Otherwise there's no reason to keep the word around and we shouldn't have to encounter
            //it again randomly. Remove the errant word and continue through the loop
            else {
                wordList.remove(wordList.get(wordIndex));
                continue;
            }
        }

        //return the compliant word
        return result;
    }
}
