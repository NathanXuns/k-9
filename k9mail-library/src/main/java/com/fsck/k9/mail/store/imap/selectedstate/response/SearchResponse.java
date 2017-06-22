package com.fsck.k9.mail.store.imap.selectedstate.response;


import java.util.ArrayList;
import java.util.List;

import com.fsck.k9.mail.store.imap.ImapResponse;
import com.fsck.k9.mail.store.imap.Responses;

import static com.fsck.k9.mail.store.imap.ImapResponseParser.equalsIgnoreCase;


public class SearchResponse extends SelectedStateResponse {

    private List<Long> numbers;

    private SearchResponse(List<ImapResponse> imapResponse) {
        super(imapResponse);
    }

    public static SearchResponse parse(List<List<ImapResponse>> imapResponses) {

        SearchResponse combinedResponse = null;
        for (List<ImapResponse> imapResponse : imapResponses) {
            SearchResponse searchResponse = new SearchResponse(imapResponse);
            if (combinedResponse == null) {
                combinedResponse = searchResponse;
            } else {
                combinedResponse.combine(searchResponse);
            }
        }

        return combinedResponse;
    }

    @Override
    void parseResponse(List<ImapResponse> imapResponses) {

        numbers = new ArrayList<>();

        for (ImapResponse response : imapResponses) {
            parseSingleLine(response, numbers);
        }
    }

    @Override
    void combine(SelectedStateResponse selectedStateResponse) {
        SearchResponse searchResponse = (SearchResponse) selectedStateResponse;
        this.numbers.addAll(searchResponse.getNumbers());
    }

    private static void parseSingleLine(ImapResponse response, List<Long> numbers) {
        if (response.isTagged() || response.size() < 2 || !equalsIgnoreCase(response.get(0), Responses.SEARCH)) {
            return;
        }

        int end = response.size();
        for (int i = 1; i < end; i++) {
            try {
                long number = response.getLong(i);
                numbers.add(number);
            } catch (NumberFormatException e) {
                return;
            }
        }
    }

    /**
     * @return A mutable list of numbers from the SEARCH response(s).
     */
    public List<Long> getNumbers() {
        return numbers;
    }

}
