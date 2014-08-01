package jp.recognize.cameraSample;

public class ColorConvert {

	public YUVtoRGB newYUVtoRGB(int w,int h,int aY,int aCr, int aCb){
		return new YUVtoRGB(w,h,aY,aCr,aCb);
	}

	public class YUVtoRGB{
		int table_RGB[][][]=null;
		int mYStep,mCrStep,mCbStep;
		int mWidth,mHeight;
		int mSize;
		public YUVtoRGB(int w,int h,int aY,int aCr, int aCb) {
			mYStep=aY;
			mCrStep=aCr;
			mCbStep=aCb;

			mWidth=w;
			mHeight=h;
			mSize=w*h;
			table_RGB=new int[256>>aY][256>>aCr][256>>aCb];
			//テーブルの生成
			for(int Y_c=-128+(1<<aY>>1);Y_c<128; Y_c+=(1<<aY)){
				int Y = Y_c; if(Y < 0) Y += 256;
				int Y_p =(Y_c&0xff)>>aY;
				for(int Cr_c=-128+(1<<aCr>>1);Cr_c<128; Cr_c+=(1<<aCr)){
					int Cr=Cr_c;
					if(Cr < 0) Cr += 128; else Cr -= 128;
					int R = Y+1402*Cr/1000;
					if(R < 0) R = 0; else if(R > 255) R = 255;
					int Cr_p=(Cr_c&0xff)>>aCr;
					for(int Cb_c=-128+(1<<aCb>>1);Cb_c<128; Cb_c+=(1<<aCb)){
						int Cb_p=(Cb_c&0xff)>>aCb;
					int Cb=Cb_c;
					if(Cb < 0) Cb += 128; else Cb -= 128;
					int B = Y+(1772*Cb)/1000;
					if(B < 0) B = 0; else if(B > 255) B = 255;
					int G = Y+(-714*Cr-344*Cb)/1000;
					if(G < 0) G = 0; else if(G > 255) G = 255;
					table_RGB[Y_p][Cr_p][Cb_p]=0xff000000 + (R << 16) + (G << 8) + B;
					}
				}
			}
		}
		public int getColorDepth(){
			return ((256>>mYStep)*(256>>mCrStep)*(256>>mCbStep));
		}
		
		
		public void get(byte[] yuv,int[] rgb){
			int i, j;
			int Cr_p,Cb_p;
			
			for(j = 0; j < mHeight; j++) {
				int pixPtr = j * mWidth;
				int cPtr =(mSize) + (j >> 1) * (mWidth);
				for(i = 0; i < mWidth; i+=2) {
					Cr_p=(yuv[cPtr ++]&0xff)>>mCrStep;
					Cb_p=(yuv[cPtr ++]&0xff)>>mCbStep;
					rgb[pixPtr] =table_RGB[(yuv[pixPtr++]&0xff)>>mYStep][Cr_p][Cb_p];
					rgb[pixPtr] =table_RGB[(yuv[pixPtr++]&0xff)>>mYStep][Cr_p][Cb_p];
				}
			}
			return;
		}
		
		
		public void getFast(byte[] yuv,int[] rgb){
			int i, j;
			int Cr_p,Cb_p;			
			int pixPtr =0;
			int cPtr =mSize;			
			for(j = 0; j < mHeight; j+=2) {//色の情報が２行おきなので２行同時に処理
				for(i = 0; i < mWidth; i+=4) {//解像度は4の倍数とする
					//２行２列を一度に処理
					Cr_p=(yuv[cPtr ++]&0xff)>>mCrStep;		Cb_p=(yuv[cPtr ++]&0xff)>>mCbStep;
					rgb[pixPtr+mWidth] =table_RGB[(yuv[pixPtr+mWidth]&0xff)>>mYStep][Cr_p][Cb_p];
					rgb[pixPtr] =table_RGB[(yuv[pixPtr++]&0xff)>>mYStep][Cr_p][Cb_p];
					rgb[pixPtr+mWidth] =table_RGB[(yuv[pixPtr+mWidth]&0xff)>>mYStep][Cr_p][Cb_p];
					rgb[pixPtr] =table_RGB[(yuv[pixPtr++]&0xff)>>mYStep][Cr_p][Cb_p];
					
					Cr_p=(yuv[cPtr ++]&0xff)>>mCrStep;		Cb_p=(yuv[cPtr ++]&0xff)>>mCbStep;
					rgb[pixPtr+mWidth] =table_RGB[(yuv[pixPtr+mWidth]&0xff)>>mYStep][Cr_p][Cb_p];
					rgb[pixPtr] =table_RGB[(yuv[pixPtr++]&0xff)>>mYStep][Cr_p][Cb_p];
					rgb[pixPtr+mWidth] =table_RGB[(yuv[pixPtr+mWidth]&0xff)>>mYStep][Cr_p][Cb_p];
					rgb[pixPtr] =table_RGB[(yuv[pixPtr++]&0xff)>>mYStep][Cr_p][Cb_p];
					
				}	
				pixPtr+=mWidth;
			}
			return;
		}
	}
}