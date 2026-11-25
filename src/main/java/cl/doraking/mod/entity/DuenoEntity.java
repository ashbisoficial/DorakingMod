package cl.doraking.mod.entity;

import cl.doraking.mod.trade.TradeConfig;
import cl.doraking.mod.trade.TradeEditorMenu;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.Level;

/** El dueno de Doraking: un comerciante que vende los dorakines por esmeraldas. */
public class DuenoEntity extends AbstractVillager {

    public DuenoEntity(EntityType<? extends AbstractVillager> type, Level level) {
        super(type, level);
        setInvulnerable(true);
        setPersistenceRequired();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0)
                .add(Attributes.MOVEMENT_SPEED, 0.5);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(1, new TradeWithPlayerGoal(this));
        goalSelector.addGoal(2, new LookAtTradingPlayerGoal(this));
        goalSelector.addGoal(3, new WaterAvoidingRandomStrollGoal(this, 0.35));
        goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 8.0f));
        goalSelector.addGoal(5, new RandomLookAroundGoal(this));
    }

    @Override
    public Component getName() {
        return Component.translatable("entity.doraking.dueno_doraking");
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (!isAlive() || isTrading()) return super.mobInteract(player, hand);
        // Agachado + clic derecho (operador o creativo): abre el editor de tradeos
        if (player.isShiftKeyDown() && (player.hasPermissions(2) || player.isCreative())) {
            if (!level().isClientSide && hand == InteractionHand.MAIN_HAND && player instanceof ServerPlayer sp) {
                TradeEditorMenu.open(sp);
            }
            return InteractionResult.sidedSuccess(level().isClientSide);
        }
        if (!level().isClientSide) {
            setTradingPlayer(player);
            openTradingScreen(player, getDisplayName(), 1);
        }
        return InteractionResult.sidedSuccess(level().isClientSide);
    }

    private int tradesVersion = -1;

    /** Siempre refleja config/doraking_trades.json (se refresca si cambia). */
    @Override
    public MerchantOffers getOffers() {
        MerchantOffers offers = super.getOffers();
        if (tradesVersion != TradeConfig.version && !isTrading()) {
            fillOffers(offers);
        }
        return offers;
    }

    private void fillOffers(MerchantOffers offers) {
        offers.clear();
        for (TradeConfig.Trade t : TradeConfig.get()) {
            if (t.b.isEmpty()) {
                offers.add(new MerchantOffer(t.a.copy(), t.result.copy(), t.maxUses, 0, 0.0f));
            } else {
                offers.add(new MerchantOffer(t.a.copy(), t.b.copy(), t.result.copy(), t.maxUses, 0, 0.0f));
            }
        }
        tradesVersion = TradeConfig.version;
    }

    /** Los tradeos salen de config/doraking_trades.json (editable en el juego). */
    @Override
    protected void updateTrades() {
        fillOffers(getOffersRaw());
    }

    private MerchantOffers getOffersRaw() {
        MerchantOffers o = new MerchantOffers();
        overrideOffers(o);
        return o;
    }

    @Override
    protected void rewardTradeXp(MerchantOffer offer) { }

    @Override
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob other) {
        return null;
    }

    @Override
    public boolean removeWhenFarAway(double distance) {
        return false;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return isTrading() ? SoundEvents.VILLAGER_TRADE : SoundEvents.VILLAGER_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(net.minecraft.world.damagesource.DamageSource src) {
        return SoundEvents.VILLAGER_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.VILLAGER_DEATH;
    }

    @Override
    protected SoundEvent getTradeUpdatedSound(boolean success) {
        return success ? SoundEvents.VILLAGER_YES : SoundEvents.VILLAGER_NO;
    }

    @Override
    public SoundEvent getNotifyTradeSound() {
        return SoundEvents.VILLAGER_YES;
    }
}
