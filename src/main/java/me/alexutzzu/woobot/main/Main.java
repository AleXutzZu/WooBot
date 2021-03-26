package me.alexutzzu.woobot.main;

import com.jagrosh.jdautilities.command.CommandClient;
import com.jagrosh.jdautilities.command.CommandClientBuilder;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import me.alexutzzu.woobot.commands.blacklist.BlacklistCommand;
import me.alexutzzu.woobot.commands.chat.MuteChatCommand;
import me.alexutzzu.woobot.commands.chat.PurgeCommand;
import me.alexutzzu.woobot.commands.help.HelpCommand;
import me.alexutzzu.woobot.commands.misc.PingCommand;
import me.alexutzzu.woobot.commands.prefix.PrefixCommand;
import me.alexutzzu.woobot.commands.setup.SetupCommand;
import me.alexutzzu.woobot.commands.tickets.CloseTicket;
import me.alexutzzu.woobot.commands.tickets.NewTicket;
import me.alexutzzu.woobot.commands.tickets.TicketAdmin;
import me.alexutzzu.woobot.commands.welcome.Welcome;
import me.alexutzzu.woobot.events.DeleteEntries;
import me.alexutzzu.woobot.events.GuildSetup;
import me.alexutzzu.woobot.events.WelcomeEvent;
import me.alexutzzu.woobot.utils.constants.Emoji;
import me.alexutzzu.woobot.utils.constants.Links;
import me.alexutzzu.woobot.utils.pojos.GuildData;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;

import java.util.EnumSet;

import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;


public class Main{
    private static final ConnectionString connectionString = new ConnectionString(System.getenv("DATABASE_URI"));
    private static final CodecRegistry pojoCodecRegistry = fromProviders(PojoCodecProvider.builder().automatic(true).build());
    private static final CodecRegistry codecRegistry = fromRegistries(MongoClientSettings.getDefaultCodecRegistry(), pojoCodecRegistry);
    private static final MongoClientSettings clientSettings = MongoClientSettings.builder().applyConnectionString(connectionString).codecRegistry(codecRegistry).build();
    private static final MongoClient mongoClient = MongoClients.create(clientSettings);
    private static final MongoDatabase mongoDatabase = mongoClient.getDatabase("Alfa_Omega_Info");


    public static void main(String[] arguments) throws Exception{
        JDA api = JDABuilder.createDefault(System.getenv("BOT_TOKEN"), (EnumSet.of(GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_MESSAGES, GatewayIntent.GUILD_MESSAGE_REACTIONS))).disableCache(CacheFlag.VOICE_STATE, CacheFlag.EMOTE).enableCache(CacheFlag.MEMBER_OVERRIDES).build();
        api.addEventListener(new GuildSetup(mongoDatabase), new WelcomeEvent(mongoDatabase), new DeleteEntries(mongoDatabase));
        EventWaiter eventWaiter = new EventWaiter();

        CommandClientBuilder builder = new CommandClientBuilder();
        builder.addCommands(new PingCommand(),
                new PrefixCommand(mongoDatabase), new Welcome(mongoDatabase),
                new PurgeCommand(), new MuteChatCommand(), new BlacklistCommand(mongoDatabase),
                new SetupCommand(mongoDatabase, eventWaiter),
                new TicketAdmin(mongoDatabase), new NewTicket(mongoDatabase, eventWaiter),
                new CloseTicket(eventWaiter)
        );

        builder.addCommand(new HelpCommand());
        builder.setServerInvite(Links.SUPPORT_SERVER.getLink());
        builder.setGuildSettingsManager(new GuildData(mongoDatabase));
        builder.setOwnerId("236873496939069443");
        builder.setPrefix(" ");
        builder.setHelpWord("help");
        builder.setActivity(Activity.playing("In development"));
        builder.setEmojis(Emoji.SUCCESS.getUnicode(), Emoji.WARNING.getUnicode(), Emoji.ERROR.getUnicode());
        builder.useHelpBuilder(false);
        CommandClient client = builder.build();
        api.addEventListener(eventWaiter);
        api.addEventListener(client);

    }
}
