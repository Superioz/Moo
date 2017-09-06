package de.superioz.moo.api.utils;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import de.superioz.moo.api.exceptions.InvalidArgumentException;
import de.superioz.moo.api.io.LanguageManager;
import de.superioz.moo.api.io.PropertiesConfig;
import javafx.util.Pair;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.text.DecimalFormat;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class StringUtil {

    public static final String SEPERATOR = "¶";
    public static final String SEPERATOR_2 = "Þ";
    public static final String SEPERATOR_3 = "þ";

    public static final Pattern SIMPLE_PLACEHOLER_REGEX = Pattern.compile("\\{[0-9]*}");
    public static final Pattern FORWARDING_PLACEHOLDER_REPLACE = Pattern.compile("\\([^\"]*\\)");
    public static final Pattern FORWARDING_PLACEHOLDER_REGEX = Pattern.compile("\\{[a-zA-Z\\-_]*}(" + FORWARDING_PLACEHOLDER_REPLACE + ")*");
    public static final Pattern DECIDING_PLACEHOLDER_REGEX = Pattern.compile("\\{\"[^\"]*\"\\|\"[^\"]*\"}");
    public static final Pattern KEY_PLACEHOLER_REGEX = Pattern.compile("%[a-zA-Z]*%");
    public static final Pattern REPLACE_REGEX = Pattern.compile(SIMPLE_PLACEHOLER_REGEX
            + "|" + KEY_PLACEHOLER_REGEX + "|" + FORWARDING_PLACEHOLDER_REGEX + "|" + DECIDING_PLACEHOLDER_REGEX);

    private static final Pattern EMPTY_STRING_CHAIN = Pattern.compile("\\n+");

    /**
     * Returns a list where all given strings are prefixed with given prefix
     *
     * @param prefix  The prefix
     * @param strings The string
     * @return The list of strings
     */
    public static String[] prefixed(String prefix, String... strings) {
        List<String> l = new ArrayList<>();
        for(String string : strings) {
            l.add(prefix + string);
        }
        return l.toArray(new String[]{});
    }

    /**
     * Cuts of the string at index if the string is longer than index
     *
     * @param s     The string to be cut off
     * @param index The index
     * @return The result string (shortened or not)
     */
    public static String cutOff(String s, int index) {
        return s.length() > index ? s.substring(0, index) : s;
    }

    /**
     * Gets a map from splitting given string.<br>
     * E.g.: String = "name=Tobias" then would the map return the key "name" the operation "=" and the value "Tobias"
     *
     * @param pattern    The pattern to search for occurences
     * @param msg        The message
     * @param f1         The function to turn a string into first type
     * @param f2         The function to turn the first type into string
     * @param f3         The function to turn a string into second type
     * @param valueRegex The regex for the value splitter
     * @param <T>        The type
     * @param <V>        The second type
     * @return The map
     * @throws InvalidArgumentException If some part is wrong
     */
    public static <T, V> Map<Pair<String, T>, Pair<V, List<String>>> getByPattern(
            Pattern pattern, String msg, Function<String, T> f1, Function<T, String> f2, Function<String, V> f3, String valueRegex)
            throws InvalidArgumentException {
        Map<Pair<String, T>, Pair<V, List<String>>> map = new HashMap<>();

        for(String s : StringUtil.find(pattern.pattern(), msg)) {
            T t = f1.apply(s);

            if(t == null) {
                throw new InvalidArgumentException(InvalidArgumentException.Type.SIMPLE, s);
            }

            String[] spl = s.split(Pattern.quote(f2.apply(t)), 2);

            if(spl.length == 2) {
                String key = spl[0];
                V v = f3.apply(spl[1]);
                String[] values = spl[1].split(valueRegex);

                map.put(new Pair<>(key, t), new Pair<>(v, Arrays.asList(values)));
            }
        }
        return map;
    }

    /**
     * Applies a decimal length with adding 0 after the '.' if necessary.<br>
     * Example: 0.34 would be to 0.3400 if the {@code decimalCount} is 4
     *
     * @param number       The number
     * @param decimalCount The decimal count
     * @return The string
     */
    public static String applyDecimalLength(double number, int decimalCount) {
        DecimalFormat df = new DecimalFormat("0." + Strings.repeat("0", decimalCount));
        return df.format(number).replace(",", ".");
    }

    /**
     * Splits the string into an array. If you choose to keep the delimiters
     * all parts (delimiter or not) will be held inside an array until the end and
     * then returned as array
     *
     * @param str            The original string to split
     * @param regex          The regex determines the delimiter
     * @param keepDelimiters Should the delimiter be kept inside the split-array
     * @return The string array
     */
    public static String[] split(String str, String regex, boolean keepDelimiters) {
        if(!keepDelimiters) {
            return split(str, regex).toArray(new String[]{});
        }
        List<String> parts = new ArrayList<>();
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(str);

        int lastEnd = 0;
        while(m.find()){
            int start = m.start();
            if(lastEnd != start) {
                String nonDelim = str.substring(lastEnd, start);
                parts.add(nonDelim);
            }
            String delim = m.group();
            parts.add(delim);

            lastEnd = m.end();
        }
        if(lastEnd != str.length()) {
            String nonDelim = str.substring(lastEnd);
            parts.add(nonDelim);
        }
        return parts.toArray(new String[]{});
    }

    /**
     * Get the most similar string out of given string list
     *
     * @param string       The string
     * @param otherStrings The string list
     * @param n            The n-gram
     * @param realistic    {@link #getSimilarities(String, List, int, boolean)}
     * @return The key of string and the similarity
     */
    public static Map.Entry<String, Double> getMostSimilar(String string, List<String> otherStrings, int n, boolean realistic) {
        Map<String, Double> similarities = getSimilarities(string, otherStrings, n, realistic);
        similarities = CollectionUtil.sortMapByValue(similarities, (o1, o2) -> o1.getValue().compareTo(o2.getValue()) * -1);

        Iterator<Map.Entry<String, Double>> iterator = similarities.entrySet().iterator();
        if(!iterator.hasNext()) return null;
        return iterator.next();
    }

    /**
     * Get all similar strings from given list by comparing them with given string.
     *
     * @param string       The original string to compare
     * @param otherStrings The list of string to search for similarities
     * @param n            The n-gram size for the {@link StringUtil#getProfile(String, int)} method
     * @param realistic    Realistic means at least the beginning letter is similar
     * @return The map of found string with similarity as double (Range: 0-1)
     */
    public static Map<String, Double> getSimilarities(String string, List<String> otherStrings, int n, boolean realistic) {
        Map<String, Double> similarities = new LinkedHashMap<>();

        for(String s : otherStrings) {
            if(s.isEmpty() || (realistic && !s.startsWith(string.substring(0, 1)))) {
                continue;
            }
            similarities.put(s, StringUtil.getSimilarity(s, string, n));
        }
        return Collections.unmodifiableMap(similarities);
    }

    /**
     * Uses the Jaccard algorithm to determine the similarity
     *
     * @param s1 The first string
     * @param s2 The second string
     * @param n  Size of the n-gram to check similarity
     * @return The Jaccard index between 0 and 1
     */
    public static double getSimilarity(String s1, String s2, int n) {
        if(s1.equals(s2)) {
            return 1;
        }
        Map<String, Integer> profile1 = getProfile(s1, n);
        Map<String, Integer> profile2 = getProfile(s2, n);

        Set<String> union = new HashSet<>();
        union.addAll(profile1.keySet());
        union.addAll(profile2.keySet());

        int inter = 0;

        for(String key : union) {
            if(profile1.containsKey(key) && profile2.containsKey(key)) {
                inter++;
            }
        }

        return 1.0 * inter / union.size();
    }

    /**
     * Splits given string into his profile as mentioned here: https://en.wikipedia.org/wiki/N-gram
     *
     * @param string The string
     * @param n      The size of the n-gram
     * @return The map of ngram and their occurence
     */
    public static Map<String, Integer> getProfile(String string, int n) {
        HashMap<String, Integer> ngrams = new HashMap<>();

        String withoutSpace = EMPTY_STRING_CHAIN.matcher(string).replaceAll(" ");
        for(int i = 0; i < (withoutSpace.length() - n + 1); i++) {
            String ngram = withoutSpace.substring(i, i + n);

            // increments occurence
            ngrams.merge(ngram, 1, (a, b) -> a + b);
        }
        return Collections.unmodifiableMap(ngrams);
    }

    /**
     * Formats given text by replacing all placeholders with given replacements<br>
     * If the placeholder contains a "abc-defg-hij" key then use the function
     * to search for a string which fits to this key.<br>
     * Can be used for {@link PropertiesConfig}
     *
     * @param text            The text
     * @param fetchUnknownKey The function if the placeholder contains a key for the property file
     * @param replacements    The replacements
     * @return The formatted string
     */
    public static String format(String text, Function<String, String> fetchUnknownKey, Object... replacements) {
        // if there are no replacements, just return the text
        if(replacements.length == 0) return text;

        // list all placeholders from the text inside a HashSet (no duplicates)
        // after getting the placeholders put them into a list (for sorting)
        // order them after this system: {0}, {1} first and then the others chronologically
        Set<String> placeHolderSet = new HashSet<>(StringUtil.find(REPLACE_REGEX, text));
        List<String> placeHolders = new ArrayList<>(placeHolderSet);

        placeHolders.sort((o1, o2) -> {
            boolean integerOrder1 = SIMPLE_PLACEHOLER_REGEX.matcher(o1).matches();
            boolean integerOrder2 = SIMPLE_PLACEHOLER_REGEX.matcher(o2).matches();

            // if both strings look like {x=number}
            if(integerOrder1 && integerOrder2) {
                return Integer.valueOf(o1.replaceAll("[{}]", ""))
                        .compareTo(Integer.valueOf(o2.replaceAll("[{}]", "")));
            }
            // if one string looks like {x=number}
            if(integerOrder1 || integerOrder2) {
                return integerOrder1 ? -1 : integerOrder2 ? 1 : 0;
            }
            return 0;
        });

        // get normal placeholders
        int replacementIndex;
        for(replacementIndex = 0; replacementIndex < placeHolders.size(); replacementIndex++) {
            String placeHolder = placeHolders.get(replacementIndex);
            Object replacement = replacementIndex >= replacements.length ? null : replacements[replacementIndex];

            if(replacement == null
                    || FORWARDING_PLACEHOLDER_REGEX.matcher(placeHolder).matches()
                    || DECIDING_PLACEHOLDER_REGEX.matcher(placeHolder).matches()) {
                continue;
            }
            text = text.replace(placeHolder, replacement + "");
        }

        // reload the text
        placeHolders = StringUtil.find(REPLACE_REGEX, text);

        // get placeholders and replace them
        for(int i = replacementIndex; i < placeHolders.size(); i++) {
            String placeHolder = placeHolders.get(i);
            Object replacement = i >= replacements.length ? null : replacements[i];

            // if the placeHolder contains an forwarding key
            if(FORWARDING_PLACEHOLDER_REGEX.matcher(placeHolder).matches()
                    && fetchUnknownKey != null) {
                List<Object> newReplacements = new ArrayList<>();
                if(replacement != null && replacement instanceof List) {
                    newReplacements.addAll((List) replacement);
                }
                for(String subReplacement : find(FORWARDING_PLACEHOLDER_REPLACE, placeHolder)) {
                    subReplacement = subReplacement.replaceAll("[()]", "");
                    newReplacements.add(subReplacement);
                }

                replacement = format(
                        fetchUnknownKey.apply(placeHolder.replaceAll("[{}]|" + FORWARDING_PLACEHOLDER_REPLACE, "")),
                        fetchUnknownKey,
                        newReplacements.toArray()
                );
            }

            // is the replacement null?
            if(replacement == null) continue;

            if(DECIDING_PLACEHOLDER_REGEX.matcher(placeHolder).matches()) {
                List<String> parts = StringUtil.find("\"[^\"]*\"", placeHolder);

                if(replacement instanceof Boolean) {
                    replacement = parts.get((Boolean) replacement ? 0 : 1).replaceAll("[\"]", "");
                }
                else if(replacement instanceof List) {
                    List l = (List) replacement;
                    Object key;

                    if(l.size() > 1 && ((key = l.get(0)) instanceof Boolean)) {
                        l = l.subList(1, l.size());
                        replacement = format(parts.get((Boolean) key ? 0 : 1).replaceAll("[\"]", ""), l.toArray());
                    }
                }
            }

            text = text.replace(placeHolder, replacement + "");
        }
        return text;
    }

    public static String format(String text, Object... replacements) {
        return format(text, s -> LanguageManager.get(s), replacements);
    }

    /**
     * Simple removes empty string from given list
     *
     * @param original The original string list
     * @return The result as list
     */
    public static List<String> removeEmpties(List<String> original) {
        List<String> l = new ArrayList<>(original);
        l.removeAll(Arrays.asList("", null));
        return l;
    }

    /**
     * Uses google#Joiner to join given seperator into given objects
     *
     * @param seperator The seperator
     * @param objects   The objects
     * @return The successful as string
     */
    public static String join(String seperator, Object... objects) {
        List<String> l = new ArrayList<>();
        for(Object o : objects) {
            String s = "null";
            if(o != null) {
                s = o.toString();
            }
            l.add(s);
        }
        return Joiner.on(seperator).join(l);
    }

    public static String join(Object... objects) {
        return join(SEPERATOR, objects);
    }

    public static String join2(Object... objects) {
        return join(SEPERATOR_2, objects);
    }

    /**
     * Uses google#Splitter to split a joined string
     *
     * @param seperator The seperator
     * @return The successful splitted string as stringList
     */
    public static List<String> split(String s, String seperator) {
        return Splitter.on(seperator).splitToList(s);
    }

    public static List<String> split(String s) {
        return split(s, SEPERATOR);
    }

    public static List<String> split2(String s) {
        return split(s, SEPERATOR_2);
    }

    /**
     * Uses google#Splitter to split a joined string without empty results
     *
     * @param seperator The seperator
     * @return The successful splitted string as stringList
     */
    public static List<String> splitWithoutEmpty(String s, String seperator) {
        return Splitter.on(seperator).omitEmptyStrings().splitToList(s);
    }

    public static List<String> splitWithoutEmpty(String s) {
        return splitWithoutEmpty(s, SEPERATOR);
    }

    public static List<String> splitWithoutEmpty2(String s) {
        return splitWithoutEmpty(s, SEPERATOR_2);
    }

    /**
     * ..
     */
    public static String toInformativeList(String header, String... format) {
        StringBuilder message = new StringBuilder(header);

        for(String s : format) {
            message.append("\n").append(s);
        }

        return message.toString();
    }

    /**
     * Turns given object list into a string list
     *
     * @param l The object list
     * @return The string list
     */
    public static List<String> toStringList(List<Object> l) {
        List<String> l1 = new ArrayList<>();
        l.forEach(object -> l1.add(object + ""));
        return l1;
    }

    /**
     * Modifies a whole string list
     *
     * @param old      The old list to be edited
     * @param function The function to modify the strings
     * @return The list of string
     */
    public static List<String> modifyStringList(List<String> old, Function<String, String> function) {
        List<String> l = new ArrayList<>();
        old.forEach(s -> l.add(function.apply(s)));
        return l;
    }

    /**
     * Gets a list to string with values and seperator.
     * Format: object $seperator object
     *
     * @param tList    The objects list
     * @param sep      The seperator (e.g. ';')
     * @param function The function
     * @param <T>      The type
     * @return The string
     */
    public static <T> String getListToString(Collection<T> tList, String sep, Function<T, String> function) {
        return String.join(sep, getStringList(tList, function));
    }

    public static <T> String getListToString(T[] tArray, String sep, Function<T, String> function) {
        return String.join(sep, getStringList(tArray, function));
    }

    /**
     * Gets a string list from given object list
     *
     * @param tList    The object list
     * @param function The function
     * @param <T>      The type
     * @return The string list
     */
    public static <T> List<String> getStringList(Collection<T> tList, Function<T, String> function) {
        List<String> l = new ArrayList<>();
        tList.forEach(t -> l.add(function.apply(t)));
        return l;
    }

    public static <T> List<String> getStringList(T[] tArray, Function<T, String> function) {
        List<String> l = new ArrayList<>();
        for(T t : tArray) {
            l.add(function.apply(t));
        }
        return l;
    }

    /**
     * Uppers the first letter
     *
     * @param str The string
     * @return The result
     */
    public static String upperFirstLetter(String str) {
        char[] stringArray = str.trim().toCharArray();
        stringArray[0] = Character.toUpperCase(stringArray[0]);
        return new String(stringArray);
    }

    /**
     * Returns the sequence appended given times
     *
     * @param sequence The string sequence to be repeated
     * @param times    How often to be repeated
     * @return The result
     */
    public static String repeat(String sequence, int times) {
        StringBuilder builder = new StringBuilder("");

        for(int i = 0; i < times; i++) {
            builder.append(sequence);
        }
        return builder.toString();
    }

    /**
     * Find matches in string from regex as filter
     *
     * @param regex  The regex
     * @param string The string to search in
     * @return The result as matches
     */
    public static List<String> find(Pattern regex, String string) {
        List<String> listMatches = new ArrayList<>();
        Matcher matcher = regex.matcher(string);

        while(matcher.find()){
            listMatches.add(matcher.group());
        }
        return listMatches;
    }

    public static List<String> find(String regex, String string) {
        return find(Pattern.compile(regex), string);
    }

    /**
     * Gets stringList forward List#toString
     *
     * @param s The list to string
     * @return The successful as stringList
     */
    public static Collection<String> fromStringifiedList(String s) {
        s = s.replace("[", "").replace("]", "");
        return Splitter.on(", ").omitEmptyStrings().splitToList(s);
    }

    /**
     * Get json string from key and val
     *
     * @param key The key
     * @param val The value
     * @return The json string (just a part tho)
     */
    public static String getJsonPart(String key, Object val) {
        return getJsonKey(key) + (val instanceof String ? ("\"" + val + "\"")
                : val != null ? val.toString().toLowerCase() : val);
    }

    public static String getJsonKey(String key) {
        return "\"" + key + "\": ";
    }

}
