package com.videoplayer.wangkly.myvideoplayer.util;
public class Util{

    /**
     * ����ٷֱ�
     * @param y
     * @param z
     * @return
     */
    public static String myPercent(int y, int z) {
        String baifenbi = "";// ���ܰٷֱȵ�ֵ
        double baiy = y * 1.0;
        double baiz = z * 1.0;
        double fen = baiy / baiz;
        DecimalFormat df1 = new DecimalFormat("##.00%");
        baifenbi = df1.format(fen);
        return baifenbi;
    }
    /**
     * ����ٷֱ�
     * @param y
     * @param z
     * @return
     */
    public static int Percent(int y, int z) {
        double baiy = y * 1.0;
        double baiz = z * 1.0;
        double fen = baiy / baiz;
        int result =new Double(fen*100).intValue();
        return result ;
    }

    /**
     * ��ȡ����ͼƬ��Դ
     *
     * @param url
     * @return
     */
    public static Bitmap getHttpBitmap(String url) {
        URL myFileURL;
        Bitmap bitmap = null;
        try {
            myFileURL = new URL(url);
            // �������
            HttpURLConnection conn = (HttpURLConnection) myFileURL.openConnection();
            // ���ó�ʱʱ��Ϊ6000���룬conn.setConnectionTiem(0);��ʾû��ʱ������
            conn.setConnectTimeout(6000);
            // �������û��������
            conn.setDoInput(true);
            // ��ʹ�û���
            conn.setUseCaches(false);
            // �����п��ޣ�û��Ӱ��
            // conn.connect();
            // �õ�������
            InputStream is = conn.getInputStream();
            // �����õ�ͼƬ
            bitmap = BitmapFactory.decodeStream(is);
            // �ر�������
            is.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return bitmap;
    }

    /**
     * ��ȡ������Ƶ����ͼ
     * @param filePath
     * @param width
     * @param height
     * @param kind
     * @return
     */
    public static Bitmap getVidioBitmap(String filePath, int width, int height,
                                        int kind) {
        Bitmap bitmap = null;
        //ThumbnailUtils��ȡͼƬ��ԭʼ������
        //Create a video thumbnail for a video
        bitmap = ThumbnailUtils.createVideoThumbnail(filePath, kind);
        //��ԭͼƬת��Ϊָ����С��
        //Creates a centered bitmap of the desired size.
        bitmap = ThumbnailUtils.extractThumbnail(bitmap, width, height,
                ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
        //�Ż�bitmap����
        return bitmap;
    }

}