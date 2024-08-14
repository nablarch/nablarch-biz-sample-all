package please.change.me.core.dataformat.convertor.datatype;

import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * EBCDICシフトコードを透過的に追加/削除するデータタイプ。<br>
 * <p>
 * 本クラスはダブルバイト文字に対してシフトイン/シフトアウトコードが付加されていない固定長ファイルの読み書きに対応するものである。<br>
 * 読み込み時は読み込んだバイト列の前後にシフトイン/シフトアウトコードを付加して文字列化を行う。<br>
 * 書き込み時は文字列からバイト列に変換した後にシフトイン/シフトアウトコードを削除する。
 * </p>
 * 
 * @author TIS
 */
public class EbcdicNoShiftCodeDoubleByteCharacterString extends EbcdicDoubleByteCharacterString {

    /**
     * 入力時に読み込んだデータを変換する。<br>
     * 読み込んだバイト列の前後にシフトアウト/シフトインコードを付加して文字列化を行う。
     *  
     * @param bytes フィールドの値データ
     * @return 変換後の値
     */
    @Override
    public String convertOnRead(byte[] bytes) {
        
        // シフトコードを付加する
        ByteBuffer buff = ByteBuffer.allocate(bytes.length + 2);
        buff.put(SHIFT_OUT);
        buff.put(bytes);
        buff.put(SHIFT_IN);
        
        return super.convertOnRead(buff.array());
    }
    
    /**
     * 出力時に書き込むデータの変換を行う。<br>
     * この実装では文字列からバイト列に変換した後にシフトアウト/シフトインコードコードを削除する。
     * 
     * @param data 書き込みを行うデータ
     * @return 変換後の値
     */
    @Override
    public byte[] convertOnWrite(Object data) {
        // super.convertOnWriteでシフトコードが付与されるため一時的にサイズを拡張
        int orgSize = getSize();
        setSize(orgSize + 2);
        byte[] bytes = super.convertOnWrite(data);
        setSize(orgSize);
        
        // シフトコードを削除する
        return Arrays.copyOfRange(bytes, 1, bytes.length - 1);
    }
}
