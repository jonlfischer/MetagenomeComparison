package edu.metagenomecomparison.presenter.Coloring;

import javafx.scene.paint.Color;


public class GradientColorScale extends ColorScale {
        private final Color MAXCOLOR;
        private final Color MINCOLOR;

        private final Color ZEROCOLOR;

        public GradientColorScale(double min, double max, Color minColor, Color maxColor) {
            this.MIN = min;
            this.MAX = max;
            this.MAXCOLOR = maxColor;
            this.MINCOLOR = minColor;
            ZEROCOLOR = null;
        }

        public GradientColorScale(double min, double max, Color minColor, Color maxColor, Color zeroColor) {
            this.MIN = min;
            this.MAX = max;
            this.MAXCOLOR = maxColor;
            this.MINCOLOR = minColor;
            ZEROCOLOR = zeroColor;
        }


        public Color getValueColor(Double value) {
            if (value == 0 && this.ZEROCOLOR != null)
                return this.ZEROCOLOR;
            if (value >= MAX)
                return MAXCOLOR;
            if (value <= MIN)
                return MINCOLOR;
            double hue = MINCOLOR.getHue() + (MAXCOLOR.getHue() - MINCOLOR.getHue()) * (value - MIN) / (MAX - MIN);
            return Color.hsb(hue, 0.5, 0.8, 1);
        }



    }
