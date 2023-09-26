package edu.metagenomecomparison.presenter.Coloring;

import javafx.scene.paint.Color;

public class SaturationColorScale extends ColorScale {

    private Color firstColor;
    private Color secondColor = null;
    public SaturationColorScale(double min, double max, Color color){
        this.MIN = min;
        this.MAX = max;
        firstColor = color;
    }

    /**
     * Saturation color scale where 0 is 0 saturation. values are assumed to be symmetrical around 0
     * @param max
     * @param minColor
     * @param maxColor
     */
    public SaturationColorScale(double max, Color minColor, Color maxColor){
        this.MIN = -max;
        this.MAX = max;
        firstColor = minColor;
        secondColor = maxColor;
    }
    @Override
    public Color getValueColor(Double value) {
        if(value == null)
            return Color.TRANSPARENT;
        double resultSaturation;
        double maxSaturation;
        if (secondColor == null){
           maxSaturation = firstColor.getSaturation();
           resultSaturation = (maxSaturation) * (value - MIN) / (MAX - MIN);
           return Color.hsb(firstColor.getHue(), resultSaturation, 0.7);
        }
        else {
            maxSaturation = Math.max(firstColor.getSaturation(), secondColor.getSaturation());
            if(value >= MAX)
                return Color.hsb(secondColor.getHue(), maxSaturation, 0.7);
            else if (value <= MIN)
                return Color.hsb(firstColor.getHue(), maxSaturation, 0.7);
            resultSaturation = maxSaturation * (value / MAX);
            if (resultSaturation >= 0)
                return Color.hsb(secondColor.getHue(), resultSaturation, 0.7);
            else
                return Color.hsb(firstColor.getHue(), -resultSaturation, 0.7);
        }
    }
}
