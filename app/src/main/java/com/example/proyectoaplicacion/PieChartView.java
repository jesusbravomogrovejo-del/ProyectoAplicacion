package com.example.proyectoaplicacion;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

public class PieChartView extends View {
    private Paint paint;
    private RectF rectF;
    private float[] valores = {0, 0, 0}; // Posiciones: [0]=OK, [1]=Próximos, [2]=Críticos
    private int[] colores = {Color.parseColor("#66BB6A"), Color.parseColor("#FFA726"), Color.parseColor("#EF5350")};

    public PieChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
        paint = new Paint();
        paint.setAntiAlias(true);

        // --- MAGIA VISUAL: ESTILO DONO ---
        paint.setStyle(Paint.Style.STROKE); // Dibuja el contorno, no rellena
        paint.setStrokeWidth(35f);          // Grosor del anillo
        paint.setStrokeCap(Paint.Cap.ROUND); // Bordes redondeados en cada segmento

        rectF = new RectF();
    }

    public void setValores(float ok, float proximos, float criticos) {
        valores[0] = ok;
        valores[1] = proximos;
        valores[2] = criticos;
        invalidate(); // Redibuja el gráfico
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float total = valores[0] + valores[1] + valores[2];

        float width = getWidth();
        float height = getHeight();
        // Restamos un margen de 40f para que el grosor de la línea no se corte en los bordes
        float radius = Math.min(width, height) / 2f - 40f;
        rectF.set(width / 2 - radius, height / 2 - radius, width / 2 + radius, height / 2 + radius);

        // Si el inventario está vacío, dibujamos un anillo gris de fondo
        if (total == 0) {
            paint.setColor(Color.parseColor("#333333"));
            canvas.drawArc(rectF, 0, 360, false, paint);
            return;
        }

        // Empezar a dibujar desde arriba (como un reloj a las 12)
        float startAngle = -90f;

        for (int i = 0; i < valores.length; i++) {
            if (valores[i] > 0) {
                float sweepAngle = (valores[i] / total) * 360f;
                paint.setColor(colores[i]);

                // Si hay más de un tipo de producto, dejamos un pequeño espacio (gap) entre colores
                float gap = (total > valores[i]) ? 5f : 0f;

                canvas.drawArc(rectF, startAngle + (gap/2), sweepAngle - gap, false, paint);
                startAngle += sweepAngle;
            }
        }
    }
}