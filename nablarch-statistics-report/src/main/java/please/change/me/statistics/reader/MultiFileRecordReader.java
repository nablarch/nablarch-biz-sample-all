package please.change.me.statistics.reader;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import nablarch.core.dataformat.DataRecord;
import nablarch.core.log.Logger;
import nablarch.core.log.LoggerManager;
import nablarch.core.util.StringUtil;
import nablarch.fw.DataReader;
import nablarch.fw.ExecutionContext;
import nablarch.fw.reader.FileDataReader;

/**
 * 複数のファイルを連続して読み込むためのファイルレコードリーダクラス。
 * <p/>
 * ファイル読み込み処理は、FileDataReaderにて行う。
 *
 * @author hisaaki sioiri
 */
public class MultiFileRecordReader implements DataReader<DataRecord> {

    /** ロガー */
    private static final Logger LOG = LoggerManager.get(MultiFileRecordReader.class);

    /** 読み込み対象のファイルリスト */
    private List<String> fileList;

    /** ファイルインデックス */
    private int fileIndex = 0;

    /** レイアウトファイル名 */
    private String layoutFile;

    /** レイアウトファイルのベースパス名 */
    private String layoutFileBasePathName;

    /** 個別のファイルを読み込むリーダ */
    private List<DataReader<DataRecord>> fileReaders;

    /** ファイルリーダ */
    private DataReader<DataRecord> reader;

    /** 入力ファイルが格納されているディレクトリのベースパス名 */
    private String dataFileBasePathName;

    /**
     * {@inheritDoc}
     * <p/>
     * 次のレコードが存在しない場合は、次のファイルを開き対象とする。
     */
    public synchronized DataRecord read(ExecutionContext ctx) {
        if (reader == null) {
            fileIndex = 0;
            createFileReaders();
            reader = fileReaders.get(fileIndex++);
        }
        if (!reader.hasNext(ctx)) {
            if (fileIndex < fileReaders.size()) {
                reader = fileReaders.get(fileIndex++);
            } else {
                return null;
            }
        }
        return reader.read(ctx);
    }

    /**
     * 読み込み対象のファイルを処理するリーダを生成する。
     */
    private void createFileReaders() {
        if (fileList == null || fileList.isEmpty()) {
            throw new IllegalStateException("data file name was blank. data file name must not be blank.");
        }

        List<DataReader<DataRecord>> readers = new ArrayList<DataReader<DataRecord>>();
        for (String file : fileList) {
            FileDataReader fileDataReader = new FileDataReader();
            if (StringUtil.hasValue(layoutFileBasePathName)) {
                fileDataReader.setLayoutFile(layoutFileBasePathName, layoutFile);
            } else {
                fileDataReader.setLayoutFile(layoutFile);
            }
            if (StringUtil.hasValue(dataFileBasePathName)) {
                fileDataReader.setDataFile(dataFileBasePathName, file);
            } else {
                fileDataReader.setDataFile(file);
            }
            readers.add(fileDataReader);
        }
        fileReaders = readers;
    }

    /**
     * {@inheritDoc}
     * <p/>
     * 次のレコードが存在しない場合には、次のファイルを開く。
     * 読み込むファイルがこれ以上存在しない場合にfalseを返却する。
     */
    public synchronized boolean hasNext(ExecutionContext ctx) {
        if (reader == null) {
            createFileReaders();
            reader = fileReaders.get(fileIndex++);
        }
        if (!reader.hasNext(ctx)) {
            if (fileIndex < fileReaders.size()) {
                reader = fileReaders.get(fileIndex++);
            } else {
                return false;
            }
        }
        return reader.hasNext(ctx);
    }

    /**
     * {@inheritDoc}
     * 全てのファイルリソースの解放処理を行う。
     */
    public void close(ExecutionContext ctx) {
        RuntimeException firstException = null;
        for (DataReader<DataRecord> fileReader : fileReaders) {
            try {
                fileReader.close(ctx);
            } catch (RuntimeException e) {
                if (firstException == null) {
                    firstException = e;
                }
                LOG.logWarn("failed in closing of the file.", e);
            }
        }
        if (firstException != null) {
            throw firstException;
        }
    }

    /**
     * フォーマット定義ファイルを設定する。
     *
     * @param layoutFile フォーマット定義ファイル
     */
    public void setLayoutFile(String layoutFile) {
        setLayoutFile(null, layoutFile);
    }

    /**
     * フォーマット定義ファイルを設定する。
     *
     * @param basePathName フォーマットファイル格納ディレクトリのベース名
     * @param layoutFile フォーマット定義ファイル
     */
    public void setLayoutFile(String basePathName, String layoutFile) {
        this.layoutFileBasePathName = basePathName;
        this.layoutFile = layoutFile;
    }

    /**
     * 読み込み対象のファイルリストを設定する。
     *
     * @param fileList 読み込み対象のファイルリスト
     */
    public void setFileList(List<String> fileList) {
        setFileList(null, fileList);
    }

    /**
     * 読み込み対象のファイルリストを設定する。
     *
     * @param basePathName 読み込み対象のファイルが格納されているディレクトリのベースパス名
     * @param fileList 読み込み対象のファイルリスト
     */
    public void setFileList(String basePathName, List<String> fileList) {
        this.dataFileBasePathName = basePathName;
        this.fileList = Collections.unmodifiableList(fileList);
    }

}

