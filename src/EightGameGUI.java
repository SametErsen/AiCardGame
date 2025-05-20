import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

public class EightGameGUI {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(GameGUI::new);
    }
}

class Card {
    String suit;
    String rank;
    int point;

    public Card(String suit, String rank) {
        this.suit = suit;
        this.rank = rank;
        this.point = switch (rank) {
            case "J", "Q", "K" -> 10;
            default -> Integer.parseInt(rank);
        };
    }

    public String toString() {
        return rank + suit;
    }
}

class Deck {
    private final List<Card> cards = new ArrayList<>();
    private final String[] suits = {"♠", "♥", "♦", "♣"};
    private final String[] ranks = {"2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K"};

    public Deck() {
        for (String suit : suits) {
            for (String rank : ranks) {
                cards.add(new Card(suit, rank));
            }
        }
        Collections.shuffle(cards);
    }

    public Card draw() {
        return cards.isEmpty() ? null : cards.remove(0);
    }
}

class Player {
    List<Card> hand = new ArrayList<>();

    public void drawCard(Card c) {
        hand.add(c);
    }

    public void discard(Card c) {
        hand.remove(c);
    }

    public int getTotalPoint() {
        return hand.stream().mapToInt(card -> card.point).sum();
    }

    public boolean hasSuitCount(String suit, int target) {
        return hand.stream().filter(c -> c.suit.equals(suit)).count() >= target;
    }

    public String mostCommonSuit() {
        Map<String, Integer> suitCount = new HashMap<>();
        for (Card c : hand) {
            suitCount.put(c.suit, suitCount.getOrDefault(c.suit, 0) + 1);
        }
        return Collections.max(suitCount.entrySet(), Map.Entry.comparingByValue()).getKey();
    }
}

class ComputerAI {
    public static Card chooseCardToDiscard(Player computer, Card newCard, int targetCount) {
        List<Card> all = new ArrayList<>(computer.hand);
        all.add(newCard);

        int bestScore = Integer.MIN_VALUE;
        Card bestToDiscard = null;


        for (Card discard : all) {
            List<Card> temp = new ArrayList<>(all);
            temp.remove(discard);
            int score = evaluateHand(temp);
            if (score > bestScore) {
                bestScore = score;
                bestToDiscard = discard;
            }
        }

        return bestToDiscard;
    }

    private static int evaluateHand(List<Card> hand) {
        int score = 0;

        Map<String, Integer> suitCount = new HashMap<>();
        for (Card c : hand) {
            suitCount.put(c.suit, suitCount.getOrDefault(c.suit, 0) + 1);
        }
        int maxSuitCount = Collections.max(suitCount.values());
        score += maxSuitCount * 10; // Aynı suit sayısı öncelikli

        int totalPoints = hand.stream().mapToInt(c -> c.point).sum();
        score -= totalPoints; // Toplam puan ne kadar azsa o kadar iyi

        return score;
    }
}

class GameGUI extends JFrame {
    Deck deck = new Deck();
    Player player = new Player();
    Player computer = new Player();
    JTextArea info = new JTextArea();
    JPanel playerPanel = new JPanel();
    JPanel computerPanel = new JPanel();
    JPanel topPanel = new JPanel();
    JButton startEasy = new JButton("Kolay Başlat");
    JButton startHard = new JButton("Zor Başlat");
    Card currentCard;
    int levelTarget = 3;

    public GameGUI() {
        setTitle("8-Game");
        setSize(800, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        Font bigFont = new Font("Arial", Font.BOLD, 18);

        startEasy.setFont(bigFont);
        startHard.setFont(bigFont);
        info.setFont(bigFont);

        topPanel.add(startEasy);
        topPanel.add(startHard);
        add(topPanel, BorderLayout.NORTH);

        info.setEditable(false);
        add(new JScrollPane(info), BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new GridLayout(2, 1));
        computerPanel.setLayout(new FlowLayout());
        playerPanel.setLayout(new FlowLayout());
        bottomPanel.add(computerPanel);
        bottomPanel.add(playerPanel);
        add(bottomPanel, BorderLayout.SOUTH);

        startEasy.addActionListener(e -> {
            topPanel.setVisible(false);
            startGame(3);
        });
        startHard.addActionListener(e -> {
            topPanel.setVisible(false);
            startGame(6);
        });

        setVisible(true);
    }

    void startGame(int target) {
        levelTarget = target;
        player.hand.clear();
        computer.hand.clear();
        deck = new Deck();
        info.setText("");
        playerPanel.removeAll();
        computerPanel.removeAll();

        for (int i = 0; i < target; i++) {
            player.drawCard(deck.draw());
            computer.drawCard(deck.draw());
        }
        drawNewCard();
    }

    void drawNewCard() {
        currentCard = deck.draw();
        if (currentCard == null) {
            endGame();
            return;
        }
        updateUI();
    }

    void updateUI() {
        playerPanel.removeAll();
        computerPanel.removeAll();
        info.append("Yeni Kart: " + currentCard + "\n");

        List<Card> temp = new ArrayList<>(player.hand);
        temp.add(currentCard);

        for (Card c : temp) {
            JButton b = new JButton(c.toString());
            b.setFont(new Font("Arial", Font.BOLD, 16));
            b.setPreferredSize(new Dimension(80, 40));
            b.addActionListener(e -> {
                player.hand.clear();
                for (Card k : temp) {
                    if (!k.toString().equals(c.toString())) player.drawCard(k);
                }
                computerTurn();
            });
            playerPanel.add(b);
        }

        JLabel compLabel = new JLabel("Bilgisayar Kartları:");
        compLabel.setFont(new Font("Arial", Font.BOLD, 18));
        computerPanel.add(compLabel);

        for (Card c : computer.hand) {
            JLabel label = new JLabel(c.toString());
            label.setFont(new Font("Arial", Font.BOLD, 16));
            label.setBorder(BorderFactory.createLineBorder(Color.BLACK));
            label.setPreferredSize(new Dimension(60, 30));
            computerPanel.add(label);
        }

        revalidate();
        repaint();
    }

    void computerTurn() {
        Card compCard = deck.draw();
        if (compCard == null) {
            endGame();
            return;
        }
        info.append("Bilgisayar yeni kart çekti: " + compCard + "\n");
        Card discard = ComputerAI.chooseCardToDiscard(computer, compCard, levelTarget);
        computer.hand.add(compCard);
        computer.hand.remove(discard);
        info.append("Bilgisayar " + discard + " kartını attı.\n");

        if (player.hasSuitCount(player.mostCommonSuit(), levelTarget)) {
            info.append("Tebrikler! Kazandınız.\n");
            disableAll();
        } else if (computer.hasSuitCount(computer.mostCommonSuit(), levelTarget)) {
            info.append("Bilgisayar kazandı.\n");
            disableAll();
        } else {
            drawNewCard();
        }
    }

    void endGame() {
        int pScore = player.getTotalPoint();
        int cScore = computer.getTotalPoint();
        info.append("Kartlar bitti.\n");
        info.append("Sizin puanınız: " + pScore + "\n");
        info.append("Bilgisayar puanı: " + cScore + "\n");
        if (pScore < cScore) info.append("Kazandınız!\n");
        else if (pScore > cScore) info.append("Kaybettiniz!\n");
        else info.append("Berabere!\n");
        disableAll();
    }

    void disableAll() {
        playerPanel.removeAll();
        computerPanel.removeAll();
        revalidate();
        repaint();
    }
}