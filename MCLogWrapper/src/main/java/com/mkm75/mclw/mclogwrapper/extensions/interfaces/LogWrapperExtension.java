package com.mkm75.mclw.mclogwrapper.extensions.interfaces;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * MCLogWrapperの拡張クラスであることを宣言します。
 * <br><br>
 * 本注釈を持つクラスはMCLogWrapperの起動時に自動で読み込まれ、
 * 保持するインターフェースに沿った動作を行います。
 * このため、MCLogWrapperのインターフェースは本注釈を持つクラスに付与する必要があります。
 * <br>
 * 拡張クラスは他と競合しない名前とバージョン、要求する他の拡張クラスを保持する必要があります。
 * また、引数無しでインスタンスを作成することが可能でなければなりません。
 * 要求クラスが循環していた場合もエラーとなります。
 * 読み込まれる時にエラーが発生したプラグインは読み込まれずに続行されます。
 * エラーログはコンソールで確認することができます。
 * <br>
 * 該当クラスは前提となるクラスが全て読み込まれたときに読み込まれます。
 * その為、コンストラクタの初期化はコンストラクタの初期化時に全て行うことができます。
 * 前提クラスが存在しない、あるいは前提クラスの読み込みに失敗した場合は読み込まれません。
 * クラスが読み込まれるのはサーバー起動前であることに注意してください。
 * 各クラスは読み込まれた時点ではワールドに干渉することができません。
 * サーバーが起動されたタイミングで処理を行うには
 * {@link com.mkm75.mclw.mclogwrapper.extensions.interfaces.ServerStateEvents ServerStateEvents}
 * を使用してください。
 * <br>
 * 拡張クラス内でのログ出力は混同を避けるために先頭に出力元をつけるなどすることを推奨します。
 * <br>
 * <b>プラグインの出力時はMCLogWrapperを含めないでください。</b>
 * <br><br>
 * 引数: <br>
 *   name 競合することのない名前<br>
 *   major_version 大まかなバージョン<br>
 *   minor_version 細かなバージョン<br>
 *   requirements_name 要求プラグインの名前<br>
 *   requirements_version 要求プラグインの大まかなバージョン<br>
 **/
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface LogWrapperExtension {
	String name();
	double major_version();
	double minor_version();
	String[] requirements_name();
	double[] requirements_version();
	String version_info_site() default "";
	boolean is_optional() default false;
}
