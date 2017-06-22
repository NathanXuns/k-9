package com.fsck.k9.mail.store.imap.selectedstate.command;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import com.fsck.k9.mail.Flag;
import com.fsck.k9.mail.MessageRetrievalListener;
import com.fsck.k9.mail.MessagingException;
import com.fsck.k9.mail.store.imap.Commands;
import com.fsck.k9.mail.store.imap.ImapConnection;
import com.fsck.k9.mail.store.imap.ImapFolder;
import com.fsck.k9.mail.store.imap.ImapMessage;
import com.fsck.k9.mail.store.imap.ImapResponse;
import com.fsck.k9.mail.store.imap.ImapUtility;
import com.fsck.k9.mail.store.imap.selectedstate.response.SearchResponse;


public class UidSearchCommand extends SelectedStateCommand {

    private boolean useUids;
    private String queryString;
    private String messageId;
    private boolean performFullTextSearch;
    private Date since;
    private Set<Flag> requiredFlags;
    private Set<Flag> forbiddenFlags;
    private MessageRetrievalListener<ImapMessage> listener;

    private UidSearchCommand(ImapConnection connection, ImapFolder folder) {
        super(connection, folder);
    }

    @Override
    public String createCommandString() {
        StringBuilder builder = new StringBuilder(Commands.UID_SEARCH).append(" ");
        if (useUids) {
            builder.append("UID ");
        }
        super.addIds(builder);
        addQueryString(builder);
        addMessageId(builder);
        addSince(builder);
        addFlags(builder, requiredFlags, false);
        addFlags(builder, forbiddenFlags, true);
        return builder.toString().trim();
    }

    @Override
    public SearchResponse execute() throws MessagingException {

        try {
            List<List<ImapResponse>> responses = executeInternal();
            return SearchResponse.parse(responses);
        } catch (IOException ioe) {
            throw folder.ioExceptionHandler(connection, ioe);
        }

    }

    private void addQueryString(StringBuilder builder) {
        if (queryString != null) {
            String encodedQuery = ImapUtility.encodeString(queryString);
            if (performFullTextSearch) {
                builder.append("TEXT ");
            } else {
                builder.append("OR SUBJECT ").append(encodedQuery).append(" FROM ");
            }
            builder.append(encodedQuery).append(" ");
        }
    }

    private void addMessageId(StringBuilder builder) {
        if (messageId != null) {
            String encodedMessageId = ImapUtility.encodeString(messageId);
            builder.append("HEADER MESSAGE-ID ").append(encodedMessageId).append(" ");
        }
    }

    private void addSince(StringBuilder builder) {
        if (since != null) {
            SimpleDateFormat rfc3501DateFormat = new SimpleDateFormat("dd-MMM-yyyy", Locale.US);
            builder.append("SINCE ").append(rfc3501DateFormat.format(since)).append(" ");
        }
    }

    private void addFlags(StringBuilder builder, Set<Flag> flagSet, boolean addNot) {

        if (flagSet != null) {
            for (Flag flag : flagSet) {
                if (addNot) {
                    builder.append("NOT ");
                }

                switch (flag) {
                    case DELETED: {
                        builder.append("DELETED ");
                        break;
                    }
                    case SEEN: {
                        builder.append("SEEN ");
                        break;
                    }
                    case ANSWERED: {
                        builder.append("ANSWERED ");
                        break;
                    }
                    case FLAGGED: {
                        builder.append("FLAGGED ");
                        break;
                    }
                    case DRAFT: {
                        builder.append("DRAFT ");
                        break;
                    }
                    case RECENT: {
                        builder.append("RECENT ");
                        break;
                    }
                    default: {
                        break;
                    }
                }
            }
        }

    }

    @Override
    Builder newBuilder() {
        return new Builder(connection, folder)
                .useUids(useUids)
                .queryString(queryString)
                .messageId(messageId)
                .performFullTextSearch(performFullTextSearch)
                .since(since)
                .requiredFlags(requiredFlags)
                .forbiddenFlags(forbiddenFlags)
                .listener(listener);
    }

    public static class Builder extends SelectedStateCommand.Builder<UidSearchCommand, Builder> {

        public Builder(ImapConnection connection, ImapFolder folder) {
            super(connection, folder);
        }

        public Builder useUids(boolean useUids) {
            command.useUids = useUids;
            return builder;
        }

        public Builder queryString(String queryString) {
            command.queryString = queryString;
            return builder;
        }

        public Builder messageId(String messageId) {
            command.messageId = messageId;
            return builder;
        }

        public Builder performFullTextSearch(boolean performFullTextSearch) {
            command.performFullTextSearch = performFullTextSearch;
            return builder;
        }

        public Builder since(Date since) {
            command.since = since;
            return builder;
        }

        public Builder requiredFlags(Set<Flag> requiredFlags) {
            command.requiredFlags = requiredFlags;
            return builder;
        }

        public Builder forbiddenFlags(Set<Flag> forbiddenFlags) {
            command.forbiddenFlags = forbiddenFlags;
            return builder;
        }

        public Builder listener(MessageRetrievalListener<ImapMessage> listener) {
            command.listener = listener;
            return builder;
        }

        @Override
        UidSearchCommand createCommand(ImapConnection connection, ImapFolder folder) {
            return new UidSearchCommand(connection, folder);
        }

        @Override
        Builder createBuilder() {
            return this;
        }

    }

}
