import java.util.stream.IntStream;

public class Test{
	public static void main(String[] args) throws Exception {
		System.out.println("Hello world! :D");

		IntStream.range(0, 3).mapToObj(i -> String.valueOf(i))
		.forEach(System.out::println);

		Thread.sleep(5000);
		
		IntStream.range(0, 4).mapToObj(i -> String.valueOf(i))
		.forEach(System.out::println);

		System.out.println("Det blir for lett! :D");

		Data c = new Data();
		c.a = 10;
		c.b = 10;
		System.out.println(c);
	}
}
