package please.change.me.core.dataformat.convertor.datatype;

import java.nio.ByteBuffer;
import java.util.Arrays;

import nablarch.core.dataformat.InvalidDataFormatException;
import nablarch.core.dataformat.convertor.datatype.DoubleByteCharacterString;

/**
 * EBCDICの全角文字を取り扱うデータタイプ。<br>
 * 
 * @author TIS
 */
public class EbcdicDoubleByteCharacterString extends DoubleByteCharacterString {

    /** シフトアウトコード */
    protected static final byte SHIFT_OUT = 0x0E;
    /** シフトインコード */
    protected static final byte SHIFT_IN = 0x0F;
    
    /** パディングに使用するバイトデータ */
    private byte[] paddingBytes;
    
    /**
     * {@inheritDoc}<br>
     * EBCDICでは全角文字にシフトアウト/シフトインコードが合計2バイト付加されるため、4バイトを返却する
     */
    @Override
    public int getPaddingCharLength() {
        return 4;
    }
    
    /**
     * 出力時に書き込むデータの変換を行う。<br>
     * この実装では文字列からバイト列に変換した後にシフトアウト/シフトインコードを先頭/終端に移動する。
     * 
     * @param data 書き込みを行うデータ
     * @return 変換後の値
     */
    @Override
    public byte[] convertOnWrite(Object data) {
        byte[] bytes = null;
        if (data == null || data.toString().length() == 0) {
            bytes = new byte[]{SHIFT_OUT, SHIFT_IN};
            return padding(bytes);
        } else {
            bytes = data.toString().getBytes(getField().getEncoding());
        }
        
        if (bytes.length > getSize()) {
            throw new InvalidDataFormatException(
                "invalid parameter was specified. "
              + "too large data. field size = '" + getSize()
              + "' data size = '"  + bytes.length  + "."
              + " data: " + data.toString()
            );
        }
        
        if (bytes[0] != SHIFT_OUT) {
          throw new InvalidDataFormatException("cannot find shift out code. data:[" + data + "]");
        }
        for (int i = 0; i < bytes.length; i++) {
            if ((i > 0) && (bytes[i] == SHIFT_OUT)) {
                throw new InvalidDataFormatException("too many shift out code. data:[" + data + "]");
            }
        }
        
        return padding(bytes);
    }

    /**
     * パディング処理を行う。
     * @param targetBytes 対象データ
     * @return パディングされたデータ
     */
    private byte[] padding(byte[] targetBytes) {
        byte[] paddingBytes = getPaddingBytes();
        
        ByteBuffer buff = ByteBuffer.wrap(new byte[getSize()]);
        buff.put(targetBytes, 0, targetBytes.length - 1);
        int padSize = (getSize() - targetBytes.length) / getPaddingBytes().length;
        for (int i = 0; i < padSize; i++) {
            buff.put(paddingBytes);
        }
        buff.put(targetBytes, targetBytes.length - 1, 1);

        return buff.array();
    }
    
    /**
     * {@inheritDoc}<br>
     * この実装ではパディングバイトからシフトコードを削除し返却する。
     */
    @Override
    protected byte[] getPaddingBytes() {
        if (paddingBytes != null) {
            return paddingBytes;
        }
        byte[] bytes = super.getPaddingBytes();
        paddingBytes = Arrays.copyOfRange(bytes, 1, bytes.length - 1);
        return paddingBytes;
    }

}
