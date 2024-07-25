package please.change.me.core.dataformat;

import nablarch.core.dataformat.DataRecordFormatter;
import nablarch.core.dataformat.FormatterFactory;

/**
 * カスタムフォーマッタファクトリ。<br>
 * 
 * デフォルトでサポートされるデータフォーマット形式以外にKeyValue形式をサポートするためのフォーマッタファクトリ。
 * 
 * @author TIS
 */
public class FormUrlEncodedDataFormatterFactory extends FormatterFactory {

    /**
     * {@inheritDoc}<br>
     * 
     * 本実装では上記のファイルタイプに加え、下記のとおりフォーマッタの生成を行い、
     * これら以外のファイルタイプの場合は例外をスローする。
     * <table border="1">
     * <tr bgcolor="#cccccc">
     * <th>ファイルタイプ</th>
     * <th>フォーマッタクラス</th>
     * </tr>
     * <tr>
     * <td>FormUrlEncoded</td>
     * <td>FormUrlEncodedDataRecordFormatter</td>
     * </tr>
     * </table>
     */
    @Override
    protected DataRecordFormatter createFormatter(String fileType,
            String formatFilePath) {
        DataRecordFormatter formatter = null;
        if ("FormUrlEncoded".equals(fileType)) {
            formatter = new FormUrlEncodedDataRecordFormatter();
        } else {
            formatter = super.createFormatter(fileType, formatFilePath); 
        }
        return formatter;
    }
}
