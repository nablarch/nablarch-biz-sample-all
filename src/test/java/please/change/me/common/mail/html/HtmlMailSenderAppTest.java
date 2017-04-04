package please.change.me.common.mail.html;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import nablarch.fw.launcher.CommandLine;
import nablarch.fw.launcher.Main;

import org.junit.Ignore;
import org.junit.Test;

/**
 * HTMLメールの機能の確認。<br />
 * オンラインなどから登録したMailを送信する機能。<br />
 *
 * 常駐化していないため、(都度起動)で動作するため、要求を逐次処理する場合はHandlerの設定を変更すること。
 *
 * @author tani takanori
 */
public class HtmlMailSenderAppTest {

    /**
     * 設定する。
     */
    public void setup() {
        /*
         * Mailを送信する環境を構築してください。
         * James 2.3を利用する場合
         *  1. アーカイブを取得し解凍する。
         *  2. bin/run.(bat|sh)でSMTPのポートを確認する。
         *  3. configのポートを2のポートにあわせる。
         *  4. telnet localhost 4555 で接続確認を行う。
         *     (windows 7ではtelnetを利用するためにコントロールパネルから利用可能にする必要があります。)
         *
         * データの準備
         *  1. dbのスキーマ、マスタデータのセットアップを行う。
         *  2. Onlineなどからメール要求を入れる。
         *
         * 機能の確認
         *  1. #sendMail() のDIコンフィグとリクエストパスを変更する。
         *  2. #sendMail()を実行する。
         *  3. ログと送信先のメールボックスを確認する。
         */
    }

    /**
     * メール送信。
     */
    @Ignore("簡易にメール送信機能を簡便に動作させることが目的なので通常は実行しない。")
    @Test
    public void sendMail() {
        CommandLine commandLine = new CommandLine(
                "-diConfig", "please/change/me/common/mail/html/mailSenderText.xml",
                "-requestPath", "please.change.me.common.mail.html.HtmlMailSender/SENDMAIL00",
                "-userId", "unused");
        int execute = Main.execute(commandLine);
        assertThat("失敗。", execute, is(0));
        /* Mailの送信状況はTest実行時のログを参照してください。*/
    }
}
