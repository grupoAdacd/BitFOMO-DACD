package es.ulpgc.dacd.businessunit.infrastructure;

public class AsciiArtBanner {

    public static void printBanner(String appName) {
        String text = appName;
        int height = 6;
        int width = text.length() * 6;
        char[][] banner = new char[height][width];
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                banner[i][j] = ' ';
            }
        }
        int position = 0;
        for (int i = 0; i < text.length(); i++) {
            drawChar(banner, text.charAt(i), position);
            position += 6;
        }
        String ANSI_YELLOW = "\u001B[33m";
        String ANSI_RESET = "\u001B[0m";
        System.out.println();
        for (int i = 0; i < height; i++) {
            System.out.print("  ");
            for (int j = 0; j < width; j++) {
                System.out.print(ANSI_YELLOW + banner[i][j] + ANSI_RESET);
            }
            System.out.println();
        }
        System.out.println();
    }

    private static void drawChar(char[][] banner, char character, int position) {
        switch (character) {
            case 'B':
                banner[0][position] = banner[0][position+1] = banner[0][position+2] = banner[0][position+3] = '█';
                banner[1][position] = banner[1][position+4] = '█';
                banner[2][position] = banner[2][position+1] = banner[2][position+2] = banner[2][position+3] = '█';
                banner[3][position] = banner[3][position+4] = '█';
                banner[4][position] = banner[4][position+4] = '█';
                banner[5][position] = banner[5][position+1] = banner[5][position+2] = banner[5][position+3] = '█';
                break;
            case 'F':
                banner[0][position+2] = banner[0][position+3] = banner[0][position+4] = banner[0][position+5] = banner[0][position+6] = '█';
                banner[1][position+2] = '█';
                banner[2][position+2] = banner[2][position+3] = banner[2][position+4] = banner[2][position+5] = '█';
                banner[3][position+2] = '█';
                banner[4][position+2] = '█';
                banner[5][position+2] = '█';
                break;
            case 'i':
                banner[1][position+2] = '█';
                banner[3][position+1] = banner[3][position+2] = '█';
                banner[4][position+2] = '█';
                banner[5][position+1] = banner[5][position+2] = banner[5][position+3] = '█';
                break;
            case 'M':
                banner[0][position+1] = banner[0][position+5] = '█';
                banner[1][position+1] = banner[1][position+2] = banner[1][position+4] = banner[1][position+5] = '█';
                banner[2][position+1] = banner[2][position+3] = banner[2][position+5] = '█';
                banner[3][position+1] = banner[3][position+5] = '█';
                banner[4][position+1] = banner[4][position+5] = '█';
                banner[5][position+1] = banner[5][position+5] = '█';
                break;
            case 'O':
                banner[0][position+3] = banner[0][position+4] = banner[0][position+2] = '█';
                banner[1][position+2] = banner[1][position+4] = '█';
                banner[2][position+2] = banner[2][position+4] = '█';
                banner[3][position+2] = banner[3][position+4] = '█';
                banner[4][position+2] = banner[4][position+4] = '█';
                banner[5][position+3] = banner[5][position+4] = banner[5][position+2] = '█';
                break;
            case 't':
                banner[1][position] = '█';
                banner[2][position] = '█';
                banner[3][position-1] = banner[3][position] = banner[3][position+1] = banner[3][position+2] = '█';
                banner[4][position] = banner[4][position+3] = '█';
                banner[5][position] = banner[5][position+1] = banner[5][position+2] = '█';
                break;
            case ' ':
                break;
            case '-':
                banner[2][position] = banner[2][position+1] = banner[2][position+2] = banner[2][position+3] = '█';
                break;
            case '_':
                banner[5][position] = banner[5][position+1] = banner[5][position+2] = banner[5][position+3] = banner[5][position+4] = '█';
                break;
            case '.':
                banner[5][position+1] = banner[5][position+2] = '█';
                break;
            default:
                banner[2][position+1] = banner[2][position+2] = '█';
                banner[3][position+1] = banner[3][position+2] = '█';
                break;
        }
    }
}