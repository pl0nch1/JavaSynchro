/* 
Файл MultiTask2wn.java
Пример апплета, в котором для рисования закрашенных прямоугольников и эллипсов
используется взаимодействие двух потоков анимации. Первый поток после рисования
прямоугольника входит в состояние ожидания и находится в нём, пока второй поток после
последовательного рисования пяти эллипсов не оповестит о необходимости выхода из 
состояния ожидания и продолжения работы. Дополнительной демонстрацией работы 
взаимодействующих потоков являются сообщения, выводимые на консоль.

Идея апплета: 
Фролов А. В., Фролов Г. В. Microsoft Visual J++. Создание приложений и апплетов на языке
Java. Часть 2. – М.: ДИАЛОГ-МИФИ, 1997. (Библиотека системного программиста; Т. 32). - с. 64
*/

import java.applet.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class MultiTask2wn extends Frame
{
    public static void main(String[] args) {
        MultiTask2wn m = new MultiTask2wn();
        m.setSize(600,605);
        m.setVisible(true);
        m.start();
    }

    private MultiTask2wn(){
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
                System.out.println("123");
                stop();
            }
        });
    }
    DrawLeft drawLeftThread = null;  // поток для рисования прямоугольников
    DrawRight drawRightThread = null;  // поток для рисования эллипсов
    int curX, curY;
    boolean animFlag;			// флаг останова анимации
    boolean drawRight;			// флаг ожидания потока рисования прямоугольников
    Dimension dimAppWndDimension;		// размеры окна апплета
    Graphics gc;				// графический контекст апплета

    //Создание и запуск потоков
    public void start()
    {
        dimAppWndDimension = getSize();
        gc = getGraphics();
        animFlag = true;
        drawRight = true;

        drawRightThread = new DrawRight();
        drawRightThread.start();
        drawLeftThread = new DrawLeft();
        drawLeftThread.start();
    }
    // Останов потоков
    public void stop()
    {
        animFlag = false;
// Оповещение ожидающего потока
        if (drawRight)
            synchronized (drawRightThread)
            {
                drawRight = false;
                drawRightThread.notify();}
        drawRightThread = null;
        drawLeftThread = null;
    }
    // Рисование фонового прямоугольника в окне апплета
    public void paint(Graphics g)
    {
        dimAppWndDimension = getSize();
        g.setColor(Color.black);
        g.fillRect(0, 0, dimAppWndDimension.width-1, dimAppWndDimension.height-1);
    }

    class DrawLeft extends Thread
    {
        public void run()
        {
            synchronized (MultiTask2wn.this) {
                try {
                    MultiTask2wn.this.wait();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            while(animFlag)
            {
                synchronized (MultiTask2wn.this)
                {
                    if (curX<=0)
                    {
                        drawRight = true;
                        MultiTask2wn.this.notify();
                        try {
                            while (drawRight)
                                MultiTask2wn.this.wait();
                        }
                        catch(InterruptedException e) {}
                    }
                }
                try {
                    Thread.sleep(5);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                float xFactor = Math.min(Math.max((float)curX/getWidth(), 0), 1);
                float yFactor = Math.min(Math.max((float)curY/getHeight(), 0), 1);
                gc.setColor(new Color(
                        255,
                        (int)(255*(xFactor)),
                        (int)(255*(yFactor))
                ));
                int randDiff = (int) (Math.random() * 10);
                int randY = (int) (Math.random()*5);
                gc.drawLine(curX, curY, curX-randDiff,curY+randY);
                curX -= randDiff;
                curY = (curY+randY) % getHeight();
            }
        }
    }

    class DrawRight extends Thread
    {
        public void run()
        {
            while(animFlag)
            {
                synchronized (MultiTask2wn.this)
                {
                    if (curX>=getWidth()) {
                        drawRight = false;
                        MultiTask2wn.this.notify();
                        try {
                            while (!drawRight)
                                MultiTask2wn.this.wait();
                        } catch (InterruptedException e) {}
                    }
                }

                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                float xFactor = Math.min(Math.max((float)curX/getWidth(), 0), 1);
                float yFactor = Math.min(Math.max((float)curY/getHeight(), 0), 1);
                gc.setColor(new Color(
                        255,
                        (int)(255*(xFactor)),
                        (int)(255*(yFactor))
                ));
                gc.drawLine(curX, curY, curX+5,curY+1);
                curX += 5;
                curY = (curY+1) % getHeight();
            }
        }
    }
}