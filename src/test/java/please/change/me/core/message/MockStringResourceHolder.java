package please.change.me.core.message;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import nablarch.core.message.StringResource;
import nablarch.core.message.StringResourceHolder;

public class MockStringResourceHolder extends StringResourceHolder {

    private Map<String, StringResource> messages = new HashMap<String, StringResource>();
    public void setMessages(String[][] messages) {
        for (String[] params: messages) {
            final String msgId = params[0];
            final Map<String, String> formats = new HashMap<String, String>();
            for (int i = 0; i * 2 + 2 <= params.length; i++) {
                formats.put(params[i * 2 + 1], params[i * 2 + 2]);
            }

            this.messages.put(msgId, new StringResource() {
                @Override
                public String getId() {
                    return msgId;
                }

                @Override
                public String getValue(Locale locale) {
                    return formats.get(locale.getLanguage());
                }
            });
        }
        
    }
    @Override
    public StringResource get(String messageId) {
        return messages.get(messageId);
    }
}
