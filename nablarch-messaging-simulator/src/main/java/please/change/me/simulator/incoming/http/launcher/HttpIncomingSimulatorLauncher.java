package please.change.me.simulator.incoming.http.launcher;

import nablarch.core.repository.SystemRepository;
import nablarch.core.repository.di.DiContainer;
import nablarch.core.repository.di.config.xml.XmlComponentDefinitionLoader;
import nablarch.fw.web.HttpServer;

/**
 * HTTPメッセージ受信シミュレータを起動するクラス。
 * <p/>
 * 下記の処理を行う。
 * 1. リポジトリ設定の読み込みとハンドラー構成の初期化
 * 2. NTF内蔵サーバ（{@link HttpServer}）の起動
 * <p/>
 * クラスパス上の {@code incoming-http-simulator-component-configuration.xml} をコンポーネント定義ファイルとして読み込んで、リポジトリを初期化する。
 * NTF内蔵サーバは、リポジトリから {@code server} というコンポーネント名で取得する。
 *
 * @author Ryo TANAKA
 * @since 1.4.2
 */
public final class HttpIncomingSimulatorLauncher {

    /** プライベートコンストラクタ。 */
    private HttpIncomingSimulatorLauncher() {
    }

    /**
     * エントリポイント。
     * @param args 実行時引数
     */
    public static void main(String... args) {
        initializeRepository();
        HttpServer server = SystemRepository.get("server");
        server.start();
    }

    /** リポジトリの初期化を行う。*/
    public static void initializeRepository() {
        // リポジトリ初期化
        DiContainer container = new DiContainer(new XmlComponentDefinitionLoader("incoming-http-simulator-component-configuration.xml"));
        SystemRepository.load(container);
    }
}
